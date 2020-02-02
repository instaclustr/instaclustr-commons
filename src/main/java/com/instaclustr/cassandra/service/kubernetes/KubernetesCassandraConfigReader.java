package com.instaclustr.cassandra.service.kubernetes;

import static java.lang.String.format;

import java.io.StringReader;
import java.util.Optional;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.instaclustr.cassandra.service.CassandraConfigReader;
import com.instaclustr.kubernetes.ConfigMapReader;
import com.instaclustr.kubernetes.SecretReader;
import io.kubernetes.client.apis.CoreV1Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KubernetesCassandraConfigReader implements CassandraConfigReader {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesCassandraConfigReader.class);

    private static final String EMPTY_CONFIG = "datastax-java-driver {}";

    private static final String SIDECAR_SECRET_NAME = "cassandra-operator-sidecar-secret";
    private static final String SIDECAR_CONFIG_NAME = "cassandra-operator-sidecar-config";
    private static final String CASSANDRA_CONFIG_NAME = "cassandra-config";

    private final Provider<CoreV1Api> coreV1ApiProvider;

    @Inject
    public KubernetesCassandraConfigReader(final Provider<CoreV1Api> coreV1ApiProvider) {
        this.coreV1ApiProvider = coreV1ApiProvider;
    }

    @Override
    public StringReader getSecretReader() {
        try {
            Optional<byte[]> bytes = new SecretReader(coreV1ApiProvider).read(SIDECAR_SECRET_NAME, CASSANDRA_CONFIG_NAME);

            if (!bytes.isPresent()) {
                throw new NullPointerException();
            }

            final String secret = new String(bytes.get());

            logger.debug(format("Read Secret %s on key %s:\n%s", SIDECAR_SECRET_NAME, CASSANDRA_CONFIG_NAME, secret));

            return new StringReader(secret);
        } catch (final Exception ex) {
            logger.warn(format("There is not secret %s with key %s, returning empty config.", SIDECAR_SECRET_NAME, CASSANDRA_CONFIG_NAME));
            return new StringReader(EMPTY_CONFIG);
        }
    }

    @Override
    public StringReader getConfigMapReader() {
        try {
            Optional<String> data = new ConfigMapReader(coreV1ApiProvider).read(SIDECAR_CONFIG_NAME, CASSANDRA_CONFIG_NAME);

            if (!data.isPresent()) {
                throw new NullPointerException();
            }

            logger.debug(format("Read ConfigMap %s on key %s:\n%s", SIDECAR_CONFIG_NAME, CASSANDRA_CONFIG_NAME, data.get()));

            return new StringReader(data.get());
        } catch (final Exception ex) {
            logger.warn(format("There is not config map %s with key %s, returning empty config.", SIDECAR_CONFIG_NAME, CASSANDRA_CONFIG_NAME));
            return new StringReader(EMPTY_CONFIG);
        }
    }
}
