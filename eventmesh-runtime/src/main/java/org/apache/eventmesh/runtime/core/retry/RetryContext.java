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

import org.apache.eventmesh.runtime.constants.EventMeshConstants;
import org.apache.eventmesh.runtime.core.retry.RetryTaskManager.ResultAttempt;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.cloudevents.CloudEvent;

public abstract class RetryContext implements Retryable {

    public CloudEvent event;

    public String seq;

    private final AtomicBoolean complete = new AtomicBoolean(Boolean.FALSE);

    private StopStrategy stopStrategy = StopStrategies.stopAfterAttempt(EventMeshConstants.DEFAULT_PUSH_RETRY_TIMES);

    private WaitStrategy waitStrategy = WaitStrategies.fixedWait(10000, TimeUnit.MILLISECONDS);

    private Attempt<Void> attempt = new ResultAttempt<>(null, 0, 0);

    public long executeTime = System.currentTimeMillis();

    public int getRetryTimes() {
        return attempt.getAttemptNumber();
    }

    public void setExecuteTime(long timeMillis) {
        this.executeTime = timeMillis;
    }

    @Override
    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    @Override
    public StopStrategy getStopStrategy() {
        return stopStrategy;
    }

    @Override
    public Attempt<Void> getAttempt() {
        return attempt;
    }

    public void setAttempt(Attempt<Void> attempt) {
        this.attempt = attempt;
    }

    protected boolean isComplete() {
        return complete.get();
    }

    protected void complete() {
        complete.compareAndSet(Boolean.FALSE, Boolean.TRUE);
    }

    public RetryContext withStopStrategy(StopStrategy stopStrategy) {
        this.stopStrategy = stopStrategy;
        return this;
    }

    public RetryContext withWaitStrategy(WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy;
        return this;
    }
}
