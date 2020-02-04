package com.instaclustr.kubernetes;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class KubernetesHelper {

    public static final String KUBERNETES_SERVICE_HOST = "KUBERNETES_SERVICE_HOST";
    public static final String KUBERNETES_SERVICE_PORT = "KUBERNETES_SERVICE_PORT";

    public static boolean isRunningInKubernetes() {
        return System.getenv(KUBERNETES_SERVICE_HOST) != null && System.getenv(KUBERNETES_SERVICE_PORT) != null;
    }

    public static boolean isRunningAsClient() {
        return Boolean.parseBoolean(System.getProperty("kubernetes.client", "false"));
    }

    public static String getHostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    public static String getPodName() throws UnknownHostException {
        return getHostname().split("\\.")[0];
    }
}
