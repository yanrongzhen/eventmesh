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

package org.apache.eventmesh.runtime.core.protocol.grpc.retry;

import org.apache.eventmesh.common.EventMeshThreadFactory;
import org.apache.eventmesh.runtime.boot.EventMeshGrpcServer;
import org.apache.eventmesh.runtime.configuration.EventMeshGrpcConfiguration;
import org.apache.eventmesh.runtime.core.retry.Retryable;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcRetryer {

    private final EventMeshGrpcConfiguration grpcConfiguration;

    public GrpcRetryer(EventMeshGrpcServer eventMeshGrpcServer) {
        this.grpcConfiguration = eventMeshGrpcServer.getEventMeshGrpcConfiguration();
    }

    private final DelayQueue<Retryable> failed = new DelayQueue<Retryable>();

    private ThreadPoolExecutor pool;

    private Thread dispatcher;

    public void pushRetry(Retryable retryable) {
        if (failed.size() >= grpcConfiguration.getEventMeshServerRetryBlockQSize()) {
            log.error("[RETRY-QUEUE] is full!");
            return;
        }
        failed.offer(retryable);
    }

    public void init() {
        pool = new ThreadPoolExecutor(
            grpcConfiguration.getEventMeshServerRetryThreadNum(),
            grpcConfiguration.getEventMeshServerRetryThreadNum(), 60000, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(grpcConfiguration.getEventMeshServerRetryBlockQSize()),
            new EventMeshThreadFactory("grpc-retry", true, Thread.NORM_PRIORITY),
            new ThreadPoolExecutor.AbortPolicy());

        dispatcher = new Thread(() -> {
            try {
                Retryable retryObj;
                while (!Thread.currentThread().isInterrupted()
                    && (retryObj = failed.take()) != null) {
                    final Retryable retryable = retryObj;
                    pool.execute(() -> {
                        try {
                            retryable.retry();
                            if (log.isDebugEnabled()) {
                                log.debug("retryObj : {}", retryable);
                            }
                        } catch (Exception e) {
                            log.error("grpc-retry-dispatcher error!", e);
                        }
                    });
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.error("grpc-retry-dispatcher error!", e);
            }
        }, "grpc-retry-dispatcher");
        dispatcher.setDaemon(true);
        log.info("GrpcRetryer inited......");
    }

    public int size() {
        return failed.size();
    }

    public void shutdown() {
        dispatcher.interrupt();
        pool.shutdown();
        log.info("GrpcRetryer shutdown......");
    }

    public void start() throws Exception {
        dispatcher.start();
        log.info("GrpcRetryer started......");
    }
}
