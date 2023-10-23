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

package org.apache.eventmesh.runtime.core.protocol.http.retry;

import org.apache.eventmesh.common.EventMeshThreadFactory;
import org.apache.eventmesh.runtime.boot.EventMeshHTTPServer;
import org.apache.eventmesh.runtime.core.protocol.AbstractRetryer;
import org.apache.eventmesh.runtime.core.protocol.DelayRetryable;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpRetryer extends AbstractRetryer {

    private final Logger retryLogger = LoggerFactory.getLogger("retry");

    private final EventMeshHTTPServer eventMeshHTTPServer;

    public HttpRetryer(EventMeshHTTPServer eventMeshHTTPServer) {
        this.eventMeshHTTPServer = eventMeshHTTPServer;
    }

    @Override
    public void pushRetry(DelayRetryable delayRetryable) {
        if (retrys.size() >= eventMeshHTTPServer.getEventMeshHttpConfiguration().getEventMeshServerRetryBlockQSize()) {
            retryLogger.error("[RETRY-QUEUE] is full!");
            return;
        }
        retrys.offer(delayRetryable);
    }

    @Override
    public void init() {
        pool = new ThreadPoolExecutor(eventMeshHTTPServer.getEventMeshHttpConfiguration().getEventMeshServerRetryThreadNum(),
            eventMeshHTTPServer.getEventMeshHttpConfiguration().getEventMeshServerRetryThreadNum(),
            60000,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(
                eventMeshHTTPServer.getEventMeshHttpConfiguration().getEventMeshServerRetryBlockQSize()),
            new EventMeshThreadFactory("http-retry", true, Thread.NORM_PRIORITY),
            new ThreadPoolExecutor.AbortPolicy());
    }

}
