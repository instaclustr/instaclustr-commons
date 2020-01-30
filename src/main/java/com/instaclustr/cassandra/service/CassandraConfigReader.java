package com.instaclustr.cassandra.service;

import java.io.StringReader;

public interface CassandraConfigReader {

    StringReader getSecretReader();

    StringReader getConfigMapReader();
}
