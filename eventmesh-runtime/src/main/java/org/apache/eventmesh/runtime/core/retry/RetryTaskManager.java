/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.runtime.core.retry;

import org.apache.eventmesh.common.EventMeshThreadFactory;
import org.apache.eventmesh.common.enums.ProtocolType;
import org.apache.eventmesh.common.protocol.SubscriptionType;
import org.apache.eventmesh.runtime.boot.AbstractRemotingServer;
import org.apache.eventmesh.runtime.core.protocol.tcp.client.session.push.DownStreamMsgContext;
import org.apache.eventmesh.runtime.util.EventMeshUtil;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryTaskManager {

    private final Logger retryLogger = LoggerFactory.getLogger("retry");

    private final AbstractRemotingServer remotingServer;

    public RetryTaskManager(AbstractRemotingServer remotingServer) {
        this.remotingServer = remotingServer;
        this.protocolType = remotingServer.getProtocolType();
    }

    private final Queue<RetryContext> failed = new LinkedBlockingDeque<>();

    private ThreadPoolExecutor pool;

    private Thread dispatcher;

    private ProtocolType protocolType;

    public void pushRetry(RetryContext retryContext) {
        if (failed.size() >= remotingServer.getConfiguration().getEventMeshServerRetryBlockQueueSize()) {
            retryLogger.error("[RETRY-QUEUE] is full!");
            return;
        }

        int maxRetryTimes = remotingServer.getConfiguration().getEventMeshMsgAsyncRetryTimes();
        if (retryContext instanceof DownStreamMsgContext) {
            DownStreamMsgContext downStreamMsgContext = (DownStreamMsgContext) retryContext;
            if (SubscriptionType.SYNC == downStreamMsgContext.getSubscriptionItem().getType()) {
                maxRetryTimes = remotingServer.getConfiguration().getEventMeshMsgSyncRetryTimes();
            }
        }

        if (retryContext.getRetryTimes() >= maxRetryTimes) {
            log.warn("pushRetry fail,retry over maxRetryTimes:{}, retryTimes:{}, seq:{}, bizSeq:{}", maxRetryTimes,
                retryContext.getRetryTimes(), retryContext.seq, EventMeshUtil.getMessageBizSeq(retryContext.event));
            return;
        }

        failed.offer(retryContext);
    }

    public void init() {
        pool = new ThreadPoolExecutor(remotingServer.getConfiguration().getEventMeshServerRetryThreadNum(),
            remotingServer.getConfiguration().getEventMeshServerRetryThreadNum(),
            60000,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(
            remotingServer.getConfiguration().getEventMeshServerRetryBlockQueueSize()),
            new EventMeshThreadFactory("http-retry", true, Thread.NORM_PRIORITY),
            new ThreadPoolExecutor.AbortPolicy());

        dispatcher = new Thread(() -> {
            try {
                RetryContext retryObj;
                while (!Thread.currentThread().isInterrupted() && (retryObj = failed.poll()) != null) {
                    if (retryLogger.isDebugEnabled()) {
                        retryLogger.debug("retryObj : {}", retryObj);
                    }
                    RetryContext retryContext = retryObj;
                    StopStrategy stopStrategy = retryContext.getStopStrategy();
                    WaitStrategy waitStrategy = retryContext.getWaitStrategy();
                    pool.execute(() -> {
                        try {
                            long startTime = System.nanoTime();
                            for (int attemptNumber = 1; ; attemptNumber++) {
                                Attempt<Void> attempt = new ResultAttempt<Void>(null, attemptNumber,
                                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
                                retryContext.retry();
                                retryContext.setExecuteTime(System.currentTimeMillis());
                                retryContext.setAttempt(attempt);
                                if (stopStrategy.shouldStop(attempt)) {
                                    break;
                                } else {
                                    long sleepTime = waitStrategy.computeSleepTime(attempt);
                                    try {
                                        Thread.sleep(sleepTime);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        throw new RetryException(attemptNumber, attempt);
                                    }
                                }
                            }
                            retryContext.complete();
                        } catch (Exception e) {
                            retryLogger.error("[{}]retry-task-dispatcher error!", protocolType, e);
                        }
                    });
                }
            } catch (Exception e) {
                retryLogger.error("[{}]retry-task-dispatcher error!", protocolType, e);
            }
        }, "retry-task-dispatcher-" + protocolType);
        dispatcher.setDaemon(true);
        log.info("[{}]Retry task inited......", protocolType);
    }

    @Immutable
    static final class ResultAttempt<R> implements Attempt<R> {
        private final R result;
        private final int attemptNumber;
        private final long delaySinceFirstAttempt;

        public ResultAttempt(R result, int attemptNumber, long delaySinceFirstAttempt) {
            this.result = result;
            this.attemptNumber = attemptNumber;
            this.delaySinceFirstAttempt = delaySinceFirstAttempt;
        }

        @Override
        public R get() throws ExecutionException {
            return result;
        }

        @Override
        public boolean hasResult() {
            return true;
        }

        @Override
        public boolean hasException() {
            return false;
        }

        @Override
        public R getResult() throws IllegalStateException {
            return result;
        }

        @Override
        public Throwable getExceptionCause() throws IllegalStateException {
            throw new IllegalStateException("The attempt resulted in a result, not in an exception");
        }

        @Override
        public int getAttemptNumber() {
            return attemptNumber;
        }

        @Override
        public long getDelaySinceFirstAttempt() {
            return delaySinceFirstAttempt;
        }
    }

    public int size() {
        return failed.size();
    }

    /**
     * Get failed queue, this method is just used for metrics.
     */
    public Queue<RetryContext> getFailedQueue() {
        return failed;
    }

    public void shutdown() {
        dispatcher.interrupt();
        pool.shutdown();
        log.info("[{}]Retry task shutdown......", protocolType);
    }

    public void start() throws Exception {
        dispatcher.start();
        log.info("[{}]Retry task started......", protocolType);
    }


    public int getRetrySize() {
        return failed.size();
    }
}
