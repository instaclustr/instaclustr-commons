package com.instaclustr.kubernetes;

import static com.instaclustr.kubernetes.KubernetesHelper.isRunningInKubernetes;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;

public class KubernetesApiModule extends AbstractModule {

    @Override
    protected void configure() {
        if (isRunningInKubernetes()) {
            bind(CoreV1Api.class).toProvider(InClusterCoreV1ApiProvider.class).in(Singleton.class);
        } else {
            bind(CoreV1Api.class).toProvider(ClientCoreV1ApiProvider.class).in(Singleton.class);
        }

        bind(KubernetesService.class).to(DefaultKubernetesService.class);
    }

    public static abstract class CustomCheckedProvider<T, E extends Exception> implements Provider<T> {

        public abstract T getChecked() throws E;

        @Override
        public T get() {
            try {
                return getChecked();
            } catch (final Exception ex) {
                throw new ProvisionException("Checked provider threw exception.", ex);
            }
        }
    }

    public static class ClientCoreV1ApiProvider extends CustomCheckedProvider<CoreV1Api, ProvisionException> {

        @Override
        public CoreV1Api getChecked() throws ProvisionException {
            try {
                ApiClient client = ClientBuilder.defaultClient();
                Configuration.setDefaultApiClient(client);
                return new CoreV1Api();
            } catch (final Exception ex) {
                throw new ProvisionException("Unable to provision client CoreV1Api object.", ex);
            }
        }
    }

    public static class InClusterCoreV1ApiProvider extends CustomCheckedProvider<CoreV1Api, ProvisionException> {

        @Override
        public CoreV1Api getChecked() throws ProvisionException {
            try {
                // loading the in-cluster config, including:
                //   1. service-account CA
                //   2. service-account bearer-token
                //   3. service-account namespace
                //   4. master endpoints(ip, port) from pre-set environment variables
                ApiClient client = ClientBuilder.cluster().build();

                // set the global default api-client to the in-cluster one from above
                Configuration.setDefaultApiClient(client);

                // the CoreV1Api loads default api-client from global configuration.
                return new CoreV1Api();
            } catch (final Exception ex) {
                throw new ProvisionException("Unable to provision in-cluster CoreV1Api object.", ex);
            }
        }
    }
}
