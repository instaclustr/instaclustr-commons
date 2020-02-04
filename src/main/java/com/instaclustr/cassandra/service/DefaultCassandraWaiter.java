package com.instaclustr.cassandra.service;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import com.datastax.oss.driver.api.core.CqlSession;
import com.google.inject.Inject;
import com.instaclustr.operations.FunctionWithEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCassandraWaiter implements CassandraWaiter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCassandraWaiter.class);

    private final CqlSessionService cqlSessionService;

    @Inject
    public DefaultCassandraWaiter(final CqlSessionService cqlSessionService) {
        this.cqlSessionService = cqlSessionService;
    }

    @Override
    public void waitUntilAvailable() {
        await().timeout(10, MINUTES)
            .pollDelay(1, MINUTES)
            .pollInterval(30, SECONDS)
            .until(() -> {
                try {
                    return cqlSessionService.doWithCqlSession(new FunctionWithEx<CqlSession, Boolean>() {
                        @Override
                        public Boolean apply(final CqlSession cqlSession) {
                            return !cqlSession.execute("SELECT * from system.local").all().isEmpty();
                        }
                    });
                } catch (final Exception ex) {
                    logger.warn("Unable to establish connection to Cassandra node.");
                    return false;
                }
            });
    }
}
