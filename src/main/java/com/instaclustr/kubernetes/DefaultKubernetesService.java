package com.instaclustr.kubernetes;

import static com.instaclustr.kubernetes.KubernetesSecretsReader.readNamespace;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Secret;

public class DefaultKubernetesService implements KubernetesService {

    private final Provider<CoreV1Api> coreV1ApiProvider;

    @Inject
    public DefaultKubernetesService(final Provider<CoreV1Api> coreV1ApiProvider) {
        this.coreV1ApiProvider = coreV1ApiProvider;
    }

    @Override
    public void deleteConfigMap(final String configMapName) throws ApiException {
        coreV1ApiProvider.get().deleteNamespacedConfigMap(configMapName, readNamespace(), null, null, null, null, null, null);
    }

    @Override
    public void deleteSecret(final String secretName) throws ApiException {
        coreV1ApiProvider.get().deleteNamespacedSecret(secretName, readNamespace(), null, null, null, null, null, null);
    }

    @Override
    public V1ConfigMap createConfigMap(final String configMapName, final String key, final String value) throws ApiException {
        return createConfigMap(configMapName, new HashMap<String, String>() {{
            put(key, value);
        }});
    }

    @Override
    public V1ConfigMap createConfigMap(final String configMapName, final Map<String, String> data) throws ApiException {
        final V1ConfigMap configMap = new V1ConfigMap();

        final V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName(configMapName);
        objectMeta.setNamespace(readNamespace());

        configMap.setMetadata(objectMeta);
        configMap.setApiVersion("v1");
        configMap.setKind("ConfigMap");
        configMap.setData(data);

        configMap.setData(data);
        return coreV1ApiProvider.get().createNamespacedConfigMap(readNamespace(), configMap, null, null, null);
    }

    @Override
    public V1Secret createSecret(final String secretName, final String key, final String value) throws ApiException {

        if (value == null) {
            throw new ApiException(format("Value for the key %s of the secret '%s' can not be null!", key, secretName));
        }

        return createSecretFromValuesAsBytes(secretName, new HashMap<String, byte[]>() {{
            put(key, value.getBytes());
        }});
    }

    @Override
    public V1Secret createSecret(final String secretName, final Map<String, String> data) throws ApiException {

        for (final Entry<String, String> entry : data.entrySet()) {
            if (entry.getKey() == null) {
                throw new ApiException(format("Value of the key of the secret '%s' can not be null!", secretName));
            }

            if (entry.getValue() == null) {
                throw new ApiException(format("Value for the key %s of the secret '%s' can not be null!", entry.getKey(), secretName));
            }
        }

        return createSecretFromValuesAsBytes(secretName, data.entrySet().stream().collect(toMap(Entry::getKey, map -> map.getValue().getBytes())));
    }

    @Override
    public V1Secret createSecretFromValuesAsBytes(final String secretName, final Map<String, byte[]> data) throws ApiException {

        for (final Entry<String, byte[]> entry : data.entrySet()) {
            if (entry.getKey() == null) {
                throw new ApiException(format("Value of the key of the secret '%s' can not be null!", secretName));
            }

            if (entry.getValue() == null) {
                throw new ApiException(format("Value for the key %s of the secret '%s' can not be null!", entry.getKey(), secretName));
            }
        }

        final V1Secret secret = new V1Secret();

        final V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName(secretName);
        objectMeta.setNamespace(readNamespace());

        secret.setMetadata(objectMeta);
        secret.setApiVersion("v1");
        secret.setType("Opaque");
        secret.setKind("Secret");
        secret.setData(data);

        return coreV1ApiProvider.get().createNamespacedSecret(readNamespace(), secret, null, null, null);
    }
}
