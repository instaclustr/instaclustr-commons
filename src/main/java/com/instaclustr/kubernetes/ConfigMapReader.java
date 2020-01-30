package com.instaclustr.kubernetes;

import static com.instaclustr.kubernetes.KubernetesSecretsReader.readNamespace;
import static java.lang.String.format;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.google.inject.Provider;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1ConfigMapList;

public class ConfigMapReader {

    private final Provider<CoreV1Api> coreV1ApiProvider;

    public ConfigMapReader(final Provider<CoreV1Api> coreV1ApiProvider) {
        this.coreV1ApiProvider = Objects.requireNonNull(coreV1ApiProvider);
    }

    public Optional<String> read(final String configMapName,
                                 final String key) throws Exception {
        return read(readNamespace(), configMapName, key);
    }

    public Optional<String> read(final String namespace,
                                 final String configMapName,
                                 final String key) throws Exception {
        return readIntoObject(namespace, configMapName, v1ConfigMap -> Optional.ofNullable(v1ConfigMap.getData().get(key)));
    }

    public <T> T readIntoObject(final String namespace,
                                final String configMapName,
                                final Function<V1ConfigMap, T> mappingFunction) throws Exception {
        final V1ConfigMapList v1ConfigMapList = coreV1ApiProvider.get().listNamespacedConfigMap(namespace, null, null, null, null, null, null, null, null);

        for (final V1ConfigMap v1ConfigMap : v1ConfigMapList.getItems()) {
            if (v1ConfigMap.getMetadata().getName().equals(configMapName)) {
                return mappingFunction.apply(v1ConfigMap);
            }
        }

        throw new IllegalStateException(format("ConfigMap with name %s was not found in namespace %s", configMapName, namespace));
    }
}
