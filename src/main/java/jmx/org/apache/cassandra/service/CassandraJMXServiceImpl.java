package jmx.org.apache.cassandra.service;

import static javax.management.JMX.newMBeanProxy;
import static jmx.org.apache.cassandra.CassandraObjectNames.STORAGE_SERVICE_MBEAN_NAME;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import jmx.org.apache.cassandra.CassandraJMXConnectionInfo;
import com.instaclustr.operations.FunctionWithEx;
import jmx.org.apache.cassandra.JMXUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraJMXServiceImpl implements CassandraJMXService {

    private static final Logger logger = LoggerFactory.getLogger(CassandraJMXService.class);

    private final CassandraJMXConnectionInfo jmxConnectionInfo;

    public CassandraJMXServiceImpl(CassandraJMXConnectionInfo jmxConnectionInfo) {
        this.jmxConnectionInfo = jmxConnectionInfo;
    }

    public <T> T doWithStorageServiceMBean(FunctionWithEx<StorageServiceMBean, T> func) throws Exception {

        JMXConnector jmxConnector = null;
        StorageServiceMBean storageServiceMBean = null;

        try {
            jmxConnector = JMXUtils.getJmxConnector(jmxConnectionInfo);

            jmxConnector.connect();

            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();

            storageServiceMBean = newMBeanProxy(mBeanServerConnection, STORAGE_SERVICE_MBEAN_NAME, StorageServiceMBean.class);

            return func.apply(storageServiceMBean);
        } finally {
            if (jmxConnector != null) {
                try {
                    jmxConnector.close();
                } catch (Exception ex) {
                    logger.error("Unable to close JMXConnector's connection.");
                } finally {
                    jmxConnector = null;
                }
            }
            if (storageServiceMBean != null) {
                storageServiceMBean = null;
            }
        }
    }
}
