package com.instaclustr.cassandra.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.instaclustr.operations.FunctionWithEx;

public interface CqlSessionService {

    CqlSession getCqlSession();

    <T> T doWithCqlSession(final FunctionWithEx<CqlSession, T> func) throws Exception;
}
