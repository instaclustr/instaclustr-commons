/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package com.instaclustr.cassandra.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.instaclustr.cassandra.CassandraModule;
import com.instaclustr.operations.FunctionWithEx;
import jmx.org.apache.cassandra.CassandraJMXConnectionInfo;
import jmx.org.apache.cassandra.service.CassandraJMXService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.StartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Ignore
public class CassandraTest {

    public static final String CASSANDRA_SERVER_DIR = "/var/lib/cassandra";
    private static final String cassandraDir = createCassandraDir();
    private static final String dockerDir = System.getProperty("docker.dir", "docker");
    private static final Consumer<CreateContainerCmd> cmd = e -> e.getHostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(9042), new ExposedPort(9042)));

    @Inject
    private CassandraJMXConnectionInfo cassandraJMXConnectionInfo;

    @Inject
    private CassandraJMXService cassandraJMXService;

    @Inject
    private CqlSessionService cqlSessionService;

    @ClassRule
    public static GenericContainer cassandra = new GenericContainer(new ImageFromDockerfile()
                                                                            .withDockerfile(Paths.get("src/test/resources/docker/Dockerfile"))
                                                                            .withFileFromPath(".", (new File(dockerDir)).toPath()))
            .withExposedPorts(9042)
            .withStartupTimeout(Duration.ofMinutes(3))
            .withCreateContainerCmdModifier(cmd)
            .withFileSystemBind(cassandraDir, CASSANDRA_SERVER_DIR, BindMode.READ_WRITE)
            .withStartupCheckStrategy(new StartupCheckStrategy() {
                @Override
                public StartupStatus checkStartupState(DockerClient dockerClient, String containerId) {
                    try (CqlSession ignored = CqlSession.builder().build()) {
                        return StartupStatus.SUCCESSFUL;
                    } catch (Exception ex) {
                        return StartupStatus.NOT_YET_KNOWN;
                    }
                }
            }.withTimeout(Duration.ofMinutes(3)))
            .withCommand("-Dcassandra.ring_delay_ms=5000 -Dcassandra.superuser_setup_delay_ms=1000");

    @Before
    public void setup() throws Exception {

        List<Module> modules = new ArrayList<Module>() {{
            add(new CassandraModule(new CassandraJMXConnectionInfo()));
        }};

        final Injector injector = Guice.createInjector(modules);

        injector.injectMembers(this);
    }

    @AfterClass
    public static void tearDownClass() {
        cassandra.stop();
    }

    @Test
    public void testCqlConnection() throws Exception {
        assertNotNull(cqlSessionService);
        assertNotNull(cassandraJMXConnectionInfo);
        assertNotNull(cassandraJMXService);

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
    }

    private static String createCassandraDir(Path path) {
        try {
            if (Files.exists(path)) {
                setPermissions(path);
                return path.toAbsolutePath().toString();
            }

            Path cassandraDir = Files.createDirectories(path);
            setPermissions(cassandraDir);
            return cassandraDir.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setPermissions(Path path) {
        try {
            // The directory will be bind-mounted into container where Cassandra runs under
            // cassandra user. Therefore we have to change permissions for all users so that
            // Cassandra from container can write into this dir.
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
                Files.setPosixFilePermissions(path, permissions);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String createCassandraDir() {
        Path tempDirectory;
        try {
            tempDirectory = Files.createTempDirectory("instaclustr-commons-test");
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create temporary directory!");
        }

        String cassandraDir = createCassandraDir(tempDirectory.toAbsolutePath());
        createCassandraDir(tempDirectory.resolve("cassandra"));
        createCassandraDir(tempDirectory.resolve("cassandra").resolve("hints"));
        createCassandraDir(tempDirectory.resolve("cassandra").resolve("data"));
        createCassandraDir(tempDirectory.resolve("cassandra").resolve("metadata"));
        createCassandraDir(tempDirectory.resolve("cassandra").resolve("commitlog"));
        createCassandraDir(tempDirectory.resolve("cassandra").resolve("cdc_raw"));
        createCassandraDir(tempDirectory.resolve("cassandra").resolve("saved_caches"));
        return cassandraDir;
    }
}
