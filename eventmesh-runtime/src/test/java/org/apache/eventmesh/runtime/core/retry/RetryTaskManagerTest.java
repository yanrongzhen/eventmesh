package org.apache.eventmesh.runtime.core.retry;

import java.util.concurrent.TimeUnit;
import org.apache.eventmesh.common.config.CommonConfiguration;
import org.apache.eventmesh.common.enums.ProtocolType;
import org.apache.eventmesh.runtime.boot.AbstractRemotingServer;
import org.junit.Test;

public class RetryTaskManagerTest {

    @Test
    public void test() throws Exception {
        AbstractRemotingServer remotingServer = new AbstractRemotingServer() {
            @Override
            public CommonConfiguration getConfiguration() {
                CommonConfiguration c = new CommonConfiguration();
                return c;
            }

            @Override
            public ProtocolType getProtocolType() {
                return ProtocolType.HTTP;
            }

            @Override
            public void start() throws Exception {

            }
        };
        RetryTaskManager manager = new RetryTaskManager(remotingServer);
        manager.init();
        manager.pushRetry(new MyContext().withWaitStrategy(
            WaitStrategies.fixedWait(1000, TimeUnit.MILLISECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(10))
        );
        manager.start();
        Thread.sleep(1000000000);
    }

    class MyContext extends RetryContext {

        @Override
        public void retry() throws Exception {
            System.out.println("retry ...");
        }
    }

}
