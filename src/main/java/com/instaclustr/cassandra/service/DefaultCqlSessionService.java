package com.instaclustr.cassandra.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.instaclustr.operations.FunctionWithEx;

public class DefaultCqlSessionService implements CqlSessionService {

    @Override
    public CqlSession getCqlSession() {
        return createCqlSession();
    }

    @Override
    public <T> T doWithCqlSession(final FunctionWithEx<CqlSession, T> func) throws Exception {
        try (final CqlSession session = createCqlSession()) {
            return func.apply(session);
        }
    }

    protected CqlSession createCqlSession() {
        return CqlSession.builder().build();
    }
}
