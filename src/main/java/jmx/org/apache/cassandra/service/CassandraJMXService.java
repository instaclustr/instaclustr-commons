package jmx.org.apache.cassandra.service;

import com.instaclustr.operations.FunctionWithEx;

public interface CassandraJMXService {

    <T> T doWithStorageServiceMBean(FunctionWithEx<StorageServiceMBean, T> func) throws Exception;
}
