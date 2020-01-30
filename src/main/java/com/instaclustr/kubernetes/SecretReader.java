package com.instaclustr.kubernetes;

import static com.instaclustr.kubernetes.KubernetesSecretsReader.readNamespace;
import static java.lang.String.format;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.google.inject.Provider;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1SecretList;

public class SecretReader {

    private final Provider<CoreV1Api> coreV1ApiProvider;

    public SecretReader(final Provider<CoreV1Api> coreV1ApiProvider) {
        this.coreV1ApiProvider = Objects.requireNonNull(coreV1ApiProvider);
    }

    public Optional<byte[]> read(final String secretName, final String key) throws Exception {
        return read(readNamespace(), secretName, key);
    }

    public Optional<byte[]> read(final String namespace, final String secretName, final String key) throws Exception {
        return readIntoObject(namespace, secretName, v1Secret -> Optional.ofNullable(v1Secret.getData().get(key)));
    }

    public <T> T readIntoObject(final String namespace, final String secretName, final Function<V1Secret, T> mappingFunction) throws Exception {
        final V1SecretList v1SecretList = coreV1ApiProvider.get().listNamespacedSecret(namespace, null, null, null, null, null, null, null, null);

        for (final V1Secret v1Secret : v1SecretList.getItems()) {
            if (v1Secret.getMetadata().getName().equals(secretName)) {
                return mappingFunction.apply(v1Secret);
            }
        }

        throw new IllegalStateException(format("Secret with name %s was not found in namespace %s", secretName, namespace));
    }
}
