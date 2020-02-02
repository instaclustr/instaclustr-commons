package com.instaclustr.cassandra.service.kubernetes;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.function.Supplier;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfig;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader;
import com.datastax.oss.driver.internal.core.config.typesafe.TypesafeDriverConfig;
import com.google.inject.Inject;
import com.instaclustr.cassandra.service.CassandraConfigReader;
import com.instaclustr.cassandra.service.CqlSessionService;
import com.instaclustr.kubernetes.KubernetesHelper;
import com.instaclustr.operations.FunctionWithEx;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesCqlSession implements CqlSessionService {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesCqlSession.class);

    private static final String CASSANDRA_RACKDC_PROPERTIES = "/tmp/cassandra-rack-config/cassandra-rackdc.properties";

    private static final String DEFAULT_DATACENTER = "datacenter1";

    private final DriverConfigLoader loader;

    @Inject
    public KubernetesCqlSession(final @Kubernetes DriverConfigLoader loader) {
        this.loader = loader;
    }

    @Override
    public CqlSession getCqlSession() {
        return createCqlSession();
    }

    @Override
    public <T> T doWithCqlSession(FunctionWithEx<CqlSession, T> func) throws Exception {
        try (final CqlSession session = createCqlSession()) {
            return func.apply(session);
        }
    }

    protected CqlSession createCqlSession() {

        final CqlSessionBuilder builder = CqlSession.builder().withConfigLoader(loader);

        if (KubernetesHelper.isRunningInKubernetes()) {
            try {
                builder.addContactPoint(new InetSocketAddress(InetAddress.getLocalHost().getHostName(), 9042));
                builder.withLocalDatacenter(getLocalDataCenter());

                return builder.build();
            } catch (final UnknownHostException ex) {
                throw new IllegalStateException("Unable to resolve hostname of the local host.", ex);
            }
        } else {
            return builder.build();
        }
    }

    private String getLocalDataCenter() {
        try {
            final Properties rackProps = new Properties();
            rackProps.load(new BufferedInputStream(new FileInputStream(CASSANDRA_RACKDC_PROPERTIES)));
            return rackProps.getProperty("dc", DEFAULT_DATACENTER);
        } catch (final Exception ex) {
            logger.error(format("Unable to read file %s to get 'dc' property!", CASSANDRA_RACKDC_PROPERTIES), ex);
            return DEFAULT_DATACENTER;
        }
    }

    public static final class KubernetesDriverConfigLoader extends DefaultDriverConfigLoader {

        private final Supplier<Config> configSupplier;

        @Inject
        public KubernetesDriverConfigLoader(final @Kubernetes CassandraConfigReader cassandraConfigReader) {
            this.configSupplier = () -> {
                ConfigFactory.invalidateCaches();
                Config config =
                    ConfigFactory.defaultOverrides()
                        .withFallback(ConfigFactory.parseReader(requireNonNull(cassandraConfigReader.getSecretReader())))
                        .withFallback(ConfigFactory.parseReader(requireNonNull(cassandraConfigReader.getConfigMapReader())))
                        .withFallback(ConfigFactory.defaultReference())
                        .resolve();
                return config.getConfig(DefaultDriverConfigLoader.DEFAULT_ROOT_PATH);
            };
        }

        @NonNull
        @Override
        public DriverConfig getInitialConfig() {
            return new TypesafeDriverConfig(getConfigSupplier().get());
        }

        @NonNull
        @Override
        public Supplier<Config> getConfigSupplier() {
            return configSupplier;
        }
    }

}
