package jmx.org.apache.cassandra.service.cassandra3;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StorageProxyMBean
{
    public long getTotalHints();
    public boolean getHintedHandoffEnabled();
    public void setHintedHandoffEnabled(boolean b);
    public void enableHintsForDC(String dc);
    public void disableHintsForDC(String dc);
    public Set<String> getHintedHandoffDisabledDCs();
    public int getMaxHintWindow();
    public void setMaxHintWindow(int ms);
    public int getMaxHintsInProgress();
    public void setMaxHintsInProgress(int qs);
    public int getHintsInProgress();

    public Long getRpcTimeout();
    public void setRpcTimeout(Long timeoutInMillis);
    public Long getReadRpcTimeout();
    public void setReadRpcTimeout(Long timeoutInMillis);
    public Long getWriteRpcTimeout();
    public void setWriteRpcTimeout(Long timeoutInMillis);
    public Long getCounterWriteRpcTimeout();
    public void setCounterWriteRpcTimeout(Long timeoutInMillis);
    public Long getCasContentionTimeout();
    public void setCasContentionTimeout(Long timeoutInMillis);
    public Long getRangeRpcTimeout();
    public void setRangeRpcTimeout(Long timeoutInMillis);
    public Long getTruncateRpcTimeout();
    public void setTruncateRpcTimeout(Long timeoutInMillis);

    public void setNativeTransportMaxConcurrentConnections(Long nativeTransportMaxConcurrentConnections);
    public Long getNativeTransportMaxConcurrentConnections();

    public void reloadTriggerClasses();

    public long getReadRepairAttempted();
    public long getReadRepairRepairedBlocking();
    public long getReadRepairRepairedBackground();

    public int getOtcBacklogExpirationInterval();
    public void setOtcBacklogExpirationInterval(int intervalInMillis);

    /** Returns each live node's schema version */
    public Map<String, List<String>> getSchemaVersions();

    public int getNumberOfTables();
}