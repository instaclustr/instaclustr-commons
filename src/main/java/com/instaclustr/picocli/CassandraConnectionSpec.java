package com.instaclustr.picocli;

import picocli.CommandLine.Option;

public class CassandraConnectionSpec {

    @Option(names = "--cassandra-hostname",
        paramLabel = "[ADDRESS]",
        defaultValue = "127.0.0.1",
        description = "Hostname of Cassandra node to connect to.")
    public String cassandraHost;

    @Option(names = "--cassandra-port",
        paramLabel = "[PORT]",
        defaultValue = "9042",
        description = "Port of Cassandra node to connect to.")
    public int cassandraPort;
}
