package jmx.org.apache.cassandra.service;

import com.instaclustr.operations.FunctionWithEx;
import jmx.org.apache.cassandra.CassandraJMXConnectionInfo;
import jmx.org.apache.cassandra.CassandraObjectNames;
import jmx.org.apache.cassandra.service.cassandra3.StorageServiceMBean;
import jmx.org.apache.cassandra.service.cassandra4.Cassandra4StorageServiceMBean;

public class CassandraJMXServiceImpl implements CassandraJMXService {

    private final CassandraJMXConnectionInfo jmxConnectionInfo;

    public CassandraJMXServiceImpl(CassandraJMXConnectionInfo jmxConnectionInfo) {
        this.jmxConnectionInfo = jmxConnectionInfo;
    }

    @Override
    public <T> T doWithCassandra4StorageServiceMBean(FunctionWithEx<Cassandra4StorageServiceMBean, T> func) throws Exception {
        return doWithStorageServiceMBean(func, Cassandra4StorageServiceMBean.class, CassandraObjectNames.V4.STORAGE_SERVICE_MBEAN_NAME, jmxConnectionInfo);
    }

    @Override
    public <T> T doWithCassandra3StorageServiceMBean(FunctionWithEx<StorageServiceMBean, T> func) throws Exception {
        return doWithStorageServiceMBean(func);
    }

    @Override
    public <T> T doWithStorageServiceMBean(FunctionWithEx<StorageServiceMBean, T> func) throws Exception {
        return doWithStorageServiceMBean(func, StorageServiceMBean.class, CassandraObjectNames.V3.STORAGE_SERVICE_MBEAN_NAME, jmxConnectionInfo);
    }
}
