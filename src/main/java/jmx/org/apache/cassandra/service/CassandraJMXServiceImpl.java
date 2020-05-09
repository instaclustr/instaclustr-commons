package jmx.org.apache.cassandra.service;

import com.instaclustr.operations.FunctionWithEx;
import jmx.org.apache.cassandra.CassandraJMXConnectionInfo;
import jmx.org.apache.cassandra.CassandraObjectNames;
import jmx.org.apache.cassandra.service.cassandra3.ColumnFamilyStoreMBean;
import jmx.org.apache.cassandra.service.cassandra3.StorageServiceMBean;
import jmx.org.apache.cassandra.service.cassandra4.Cassandra4ColumnFamilyStoreMBean;
import jmx.org.apache.cassandra.service.cassandra4.Cassandra4StorageServiceMBean;

public class CassandraJMXServiceImpl implements CassandraJMXService {

    private final CassandraJMXConnectionInfo jmxConnectionInfo;

    public CassandraJMXServiceImpl(CassandraJMXConnectionInfo jmxConnectionInfo) {
        this.jmxConnectionInfo = jmxConnectionInfo;
    }

    @Override
    public <T> T doWithCassandra4StorageServiceMBean(final FunctionWithEx<Cassandra4StorageServiceMBean, T> func) throws Exception {
        return doWithMBean(func,
                           Cassandra4StorageServiceMBean.class,
                           CassandraObjectNames.V4.STORAGE_SERVICE_MBEAN_NAME,
                           jmxConnectionInfo);
    }

    @Override
    public <T> T doWithCassandra3StorageServiceMBean(final FunctionWithEx<StorageServiceMBean, T> func) throws Exception {
        return doWithStorageServiceMBean(func);
    }

    @Override
    public <T> T doWithStorageServiceMBean(final FunctionWithEx<StorageServiceMBean, T> func) throws Exception {
        return doWithMBean(func,
                           StorageServiceMBean.class,
                           CassandraObjectNames.V3.STORAGE_SERVICE_MBEAN_NAME,
                           jmxConnectionInfo);
    }

    @Override
    public <T> T doWithCassandra3ColumnFamilyStoreMBean(final FunctionWithEx<ColumnFamilyStoreMBean, T> func,
                                                        final String keyspace,
                                                        final String columnFamily) throws Exception {
        return doWithMBean(func, ColumnFamilyStoreMBean.class,
                           getColumnFamilyMBeanObjectNameQuery(keyspace, columnFamily),
                           jmxConnectionInfo);
    }

    @Override
    public <T> T doWithCassandra4ColumnFamilyStoreMBean(final FunctionWithEx<Cassandra4ColumnFamilyStoreMBean, T> func,
                                                        final String keyspace,
                                                        final String columnFamily) throws Exception {
        return doWithMBean(func,
                           Cassandra4ColumnFamilyStoreMBean.class,
                           getColumnFamilyMBeanObjectNameQuery(keyspace, columnFamily),
                           jmxConnectionInfo);
    }

    @Override
    public CassandraJMXConnectionInfo getCassandraJmxConnectionInfo() {
        return jmxConnectionInfo;
    }

    private String getColumnFamilyMBeanObjectNameQuery(final String keyspace, final String columnFamily) {
        final String type = columnFamily.contains(".") ? "IndexColumnFamilies" : "ColumnFamilies";
        return "org.apache.cassandra.db:type=*" + type + ",keyspace=" + keyspace + ",columnfamily=" + columnFamily;
    }
}
