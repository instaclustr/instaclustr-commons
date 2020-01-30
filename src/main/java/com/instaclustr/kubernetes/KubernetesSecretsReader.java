package com.instaclustr.kubernetes;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesSecretsReader {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesSecretsReader.class);

    public static KubernetesSecrets read() throws Exception {
        final KubernetesSecrets secrets = new KubernetesSecrets();

        secrets.cacrt = KubernetesSecretsReader.readCaCrt();
        secrets.token = KubernetesSecretsReader.readNamespace();
        secrets.token = KubernetesSecretsReader.readToken();

        return secrets;
    }

    public static String readNamespace() {
        try {
            return readPath("/var/run/secrets/kubernetes.io/serviceaccount/namespace");
        } catch (final Exception ex) {
            logger.warn("Unable to read namespace, returning 'default' namespace.");
            return "default";
        }
    }

    public static String readToken() throws Exception {
        return readPath("/var/run/secrets/kubernetes.io/serviceaccount/token");
    }

    public static String readCaCrt() throws Exception {
        return readPath("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt");
    }

    private static String readPath(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}
