package com.instaclustr.cassandra;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.instaclustr.cassandra.service.CassandraConfigReader;
import com.instaclustr.cassandra.service.CqlSessionService;
import com.instaclustr.cassandra.service.DefaultCqlSessionService;
import com.instaclustr.cassandra.service.kubernetes.Kubernetes;
import com.instaclustr.cassandra.service.kubernetes.KubernetesCassandraConfigReader;
import com.instaclustr.cassandra.service.kubernetes.KubernetesCqlSession;
import com.instaclustr.cassandra.service.kubernetes.KubernetesCqlSession.KubernetesDriverConfigLoader;
import com.instaclustr.kubernetes.KubernetesHelper;
import com.instaclustr.operations.FunctionWithEx;
import jmx.org.apache.cassandra.CassandraJMXConnectionInfo;
import jmx.org.apache.cassandra.service.CassandraJMXService;
import jmx.org.apache.cassandra.service.CassandraJMXServiceImpl;
import jmx.org.apache.cassandra.service.StorageServiceMBean;

public class CassandraModule extends AbstractModule {

    private final CassandraJMXConnectionInfo jmxConnectionInfo;

    public CassandraModule(final CassandraJMXConnectionInfo jmxConnectionInfo) {
        this.jmxConnectionInfo = jmxConnectionInfo;
    }

    @Override
    protected void configure() {
        if (KubernetesHelper.isRunningInKubernetes() || KubernetesHelper.isRunningAsClient()) {
            bind(CqlSessionService.class).to(KubernetesCqlSession.class);
            bind(DriverConfigLoader.class).annotatedWith(Kubernetes.class).to(KubernetesDriverConfigLoader.class);
            bind(CassandraConfigReader.class).annotatedWith(Kubernetes.class).to(KubernetesCassandraConfigReader.class);
        } else {
            bind(CqlSessionService.class).to(DefaultCqlSessionService.class);
        }
    }

    @Singleton
    @Provides
    CassandraJMXConnectionInfo provideJmxConnectionInfo() {
        return jmxConnectionInfo;
    }

    @Singleton
    @Provides
    CassandraJMXService provideCassandraJMXService(final CassandraJMXConnectionInfo jmxConnectionInfo) {
        return new CassandraJMXServiceImpl(jmxConnectionInfo);
    }

    @Provides
    @Singleton
    CassandraVersion provideCassandraVersion(final CassandraJMXService cassandraJMXService) throws Exception {
        return CassandraVersion.parse(cassandraJMXService.doWithStorageServiceMBean(new FunctionWithEx<StorageServiceMBean, String>() {
            @Override
            public String apply(StorageServiceMBean object) {
                return object.getReleaseVersion();
            }
        }));
    }
}
