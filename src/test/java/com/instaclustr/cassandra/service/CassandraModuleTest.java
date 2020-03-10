package com.instaclustr.cassandra.service;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.artifact.Artifact;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.instaclustr.cassandra.CassandraModule;
import com.instaclustr.kubernetes.KubernetesApiModule;
import com.instaclustr.operations.FunctionWithEx;
import jmx.org.apache.cassandra.CassandraJMXConnectionInfo;
import jmx.org.apache.cassandra.service.CassandraJMXService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CassandraModuleTest {

    @Inject
    private CassandraJMXConnectionInfo cassandraJMXConnectionInfo;

    @Inject
    private CassandraJMXService cassandraJMXService;

    @Inject
    private CqlSessionService cqlSessionService;

    @BeforeMethod
    public void setup() throws Exception {

        System.setProperty("kubernetes.client", "false");

        List<Module> modules = new ArrayList<Module>() {{
            add(new CassandraModule(new CassandraJMXConnectionInfo()));
            add(new KubernetesApiModule());
        }};

        final Injector injector = Guice.createInjector(modules);

        injector.injectMembers(this);
    }

    @Test
    public void test() throws Exception {
        assertNotNull(cqlSessionService);
        assertNotNull(cassandraJMXConnectionInfo);
        assertNotNull(cassandraJMXService);

        EmbeddedCassandraFactory embeddedCassandraFactory = new EmbeddedCassandraFactory();
        embeddedCassandraFactory.setArtifact(Artifact.ofVersion("3.11.6"));
        embeddedCassandraFactory.getJvmOptions().add("-Xmx1g");
        embeddedCassandraFactory.getJvmOptions().add("-Xms1g");
        Cassandra cassandraToBackup = embeddedCassandraFactory.create();

        cassandraToBackup.start();

        try {
            int rows = cqlSessionService.doWithCqlSession(new FunctionWithEx<CqlSession, Integer>() {
                @Override
                public Integer apply(final CqlSession session) throws Exception {
                    ResultSet resultSet = session.execute("SELECT * from system.local;");

                    assertNotNull(resultSet);

                    final List<Row> rows = resultSet.all();

                    assertFalse(rows.isEmpty());

                    assertEquals(rows.get(0).getString(0), "local");

                    return rows.size();
                }
            });

            assertEquals(rows, 1);
        } finally {
            cassandraToBackup.stop();
        }
    }

    public static class DummyCassandraConfigReader implements CassandraConfigReader {

        @Override
        public StringReader getSecretReader() {
            return new StringReader("datastax-java-driver {}");
        }

        @Override
        public StringReader getConfigMapReader() {
            return new StringReader("datastax-java-driver {}");
        }
    }
}
