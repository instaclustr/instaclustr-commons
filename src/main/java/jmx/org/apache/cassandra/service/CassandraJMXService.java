package jmx.org.apache.cassandra.service;

import static javax.management.JMX.newMBeanProxy;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import com.instaclustr.operations.FunctionWithEx;
import jmx.org.apache.cassandra.CassandraJMXConnectionInfo;
import jmx.org.apache.cassandra.JMXUtils;
import jmx.org.apache.cassandra.service.cassandra3.StorageServiceMBean;
import jmx.org.apache.cassandra.service.cassandra4.Cassandra4StorageServiceMBean;

public interface CassandraJMXService {

    <T> T doWithCassandra4StorageServiceMBean(FunctionWithEx<Cassandra4StorageServiceMBean, T> func) throws Exception;

    <T> T doWithCassandra3StorageServiceMBean(FunctionWithEx<StorageServiceMBean, T> func) throws Exception;

    <T> T doWithStorageServiceMBean(FunctionWithEx<StorageServiceMBean, T> func) throws Exception;

    default <T, U> T doWithStorageServiceMBean(FunctionWithEx<U, T> func,
                                               Class<U> mbeanClass,
                                               ObjectName objectName,
                                               CassandraJMXConnectionInfo jmxConnectionInfo) throws Exception {

        try (JMXConnector jmxConnector = JMXUtils.getJmxConnector(jmxConnectionInfo)) {

            jmxConnector.connect();

            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();

            return func.apply(newMBeanProxy(mBeanServerConnection, objectName, mbeanClass));
        }
    }
}
