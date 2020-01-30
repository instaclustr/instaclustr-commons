package com.instaclustr.cassandra.service.kubernetes;

import static java.util.Objects.requireNonNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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

public class KubernetesCqlSession implements CqlSessionService {

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
                return builder.addContactPoint(new InetSocketAddress(InetAddress.getLocalHost().getHostName(), 9042)).build();
            } catch (final UnknownHostException ex) {
                throw new IllegalStateException("Unable to resolve hostname of the local host.", ex);
            }
        } else {
            return builder.build();
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
