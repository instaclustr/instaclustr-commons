package com.instaclustr.kubernetes;

import java.util.Map;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1Secret;

public interface KubernetesService {

    void deleteConfigMap(final String configMapName) throws ApiException;

    void deleteSecret(final String secretName) throws ApiException;

    V1ConfigMap createConfigMap(final String configMapName, final String key, final String value) throws ApiException;

    V1ConfigMap createConfigMap(final String configMapName, final Map<String, String> data) throws ApiException;

    V1Secret createSecret(final String secretName, final String key, final String value) throws ApiException;

    V1Secret createSecret(final String secretName, final Map<String, String> data) throws ApiException;

    V1Secret createSecretFromValuesAsBytes(final String secretName, final Map<String, byte[]> data) throws ApiException;
}
