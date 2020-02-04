package com.instaclustr.cassandra.service.kubernetes;

import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraKubernetesHelper {

    private static final Logger logger = LoggerFactory.getLogger(CassandraKubernetesHelper.class);

    public static final String CASSANDRA_RACKDC_PROPERTIES = "/tmp/cassandra-rack-config/cassandra-rackdc.properties";
    public static final String DEFAULT_DATACENTER = "datacenter1";

    private static final String SIDECAR_SECRET_NAME = "cassandra-operator-sidecar-secret";
    private static final String SIDECAR_CONFIG_NAME = "cassandra-operator-sidecar-config";
    public static final String CASSANDRA_CONFIG_NAME = "cassandra-config";

    public static String getLocalDataCenter() {
        try {
            final Properties rackProps = new Properties();
            rackProps.load(new BufferedInputStream(new FileInputStream(CASSANDRA_RACKDC_PROPERTIES)));
            return rackProps.getProperty("dc", DEFAULT_DATACENTER);
        } catch (final Exception ex) {
            logger.error(format("Unable to read file %s to get 'dc' property!", CASSANDRA_RACKDC_PROPERTIES), ex);
            return DEFAULT_DATACENTER;
        }
    }

    public static String getCassandraSidecarSecretName() {
        return SIDECAR_SECRET_NAME + "-" + getLocalDataCenter();
    }

    public static String getCassandraSidecarConfigMapName() {
        return SIDECAR_CONFIG_NAME + "-" + getLocalDataCenter();
    }
}
