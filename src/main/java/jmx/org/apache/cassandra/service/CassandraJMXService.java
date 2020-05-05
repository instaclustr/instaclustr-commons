package jmx.org.apache.cassandra.service;

import static javax.management.JMX.newMBeanProxy;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import java.util.Set;

import com.instaclustr.operations.FunctionWithEx;
import jmx.org.apache.cassandra.CassandraJMXConnectionInfo;
import jmx.org.apache.cassandra.JMXUtils;
import jmx.org.apache.cassandra.service.cassandra3.ColumnFamilyStoreMBean;
import jmx.org.apache.cassandra.service.cassandra3.StorageServiceMBean;
import jmx.org.apache.cassandra.service.cassandra4.Cassandra4ColumnFamilyStoreMBean;
import jmx.org.apache.cassandra.service.cassandra4.Cassandra4StorageServiceMBean;

public interface CassandraJMXService {

    // storage service mbean

    <T> T doWithCassandra4StorageServiceMBean(FunctionWithEx<Cassandra4StorageServiceMBean, T> func) throws Exception;

    <T> T doWithCassandra3StorageServiceMBean(FunctionWithEx<StorageServiceMBean, T> func) throws Exception;

    <T> T doWithStorageServiceMBean(FunctionWithEx<StorageServiceMBean, T> func) throws Exception;

    // column family store mbean

    <T> T doWithCassandra3ColumnFamilyStoreMBean(FunctionWithEx<ColumnFamilyStoreMBean, T> func, String keyspace, String columnFamily) throws Exception;

    <T> T doWithCassandra4ColumnFamilyStoreMBean(FunctionWithEx<Cassandra4ColumnFamilyStoreMBean, T> func, String keyspace, String columnFamily) throws Exception;

    default <T, U> T doWithMBean(FunctionWithEx<U, T> func,
                                 Class<U> mbeanClass,
                                 ObjectName objectName,
                                 CassandraJMXConnectionInfo jmxConnectionInfo) throws Exception {

        try (JMXConnector jmxConnector = JMXUtils.getJmxConnector(jmxConnectionInfo)) {

            jmxConnector.connect();

            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();

            return func.apply(newMBeanProxy(mBeanServerConnection, objectName, mbeanClass));
        }
    }

    default <T, U> T doWithMBean(FunctionWithEx<U, T> func,
                                 Class<U> mbeanClass,
                                 String query,
                                 CassandraJMXConnectionInfo jmxConnectionInfo) throws Exception {

        try (JMXConnector jmxConnector = JMXUtils.getJmxConnector(jmxConnectionInfo)) {

            jmxConnector.connect();

            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();

            Set<ObjectName> objectNames = mBeanServerConnection.queryNames(new ObjectName(query), null);

            if (objectNames.isEmpty()) {
                throw new IllegalStateException(String.format("Could not find ObjectName with query %s", query));
            }

            if (objectNames.size() != 1) {
                throw new IllegalStateException(String.format("There is more than one ObjectName returned by query %s. They are: %s",
                                                              query,
                                                              objectNames.stream().map(ObjectName::getCanonicalName)));
            }

            return func.apply(newMBeanProxy(mBeanServerConnection, objectNames.iterator().next(), mbeanClass));
        }
    }
}
