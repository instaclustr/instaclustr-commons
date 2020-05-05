package com.instaclustr.operations;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.instaclustr.threading.Executors;
import com.instaclustr.threading.Executors.ExecutorServiceSupplier;

public class OperationsService extends AbstractIdleService {

    private final ListeningExecutorService executorService;
    private final Map<Class<? extends OperationRequest>, OperationFactory> operationFactoriesByRequestType;
    private final Map<UUID, Operation> operations;

    @Inject
    public OperationsService(final Map<Class<? extends OperationRequest>, OperationFactory> operationFactoriesByRequestType,
                             final @OperationsMap Map<UUID, Operation> operations,
                             final ExecutorServiceSupplier executorServiceSupplier) {
        this.operationFactoriesByRequestType = operationFactoriesByRequestType;
        this.operations = operations;
        this.executorService = executorServiceSupplier.get(Integer.parseInt(System.getProperty("instaclustr.commons.operations.executor.size",
                                                                                               Executors.DEFAULT_CONCURRENT_CONNECTIONS.toString())));
    }

    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void shutDown() throws Exception {
        MoreExecutors.shutdownAndAwaitTermination(executorService, 1, TimeUnit.MINUTES);
    }

    public void submitOperation(final Operation operation) {
        operations.put(operation.id, operation);
        executorService.submit(operation);
    }

    public Operation submitOperationRequest(final OperationRequest request) {
        final OperationFactory operationFactory = operationFactoriesByRequestType.get(request.getClass());

        final Operation operation = operationFactory.createOperation(request);

        submitOperation(operation);

        return operation;
    }

    public Map<UUID, Operation> operations() {
        return Collections.unmodifiableMap(operations);
    }

    public Optional<Operation> operation(final UUID id) {
        return Optional.ofNullable(operations.get(id));
    }
}
