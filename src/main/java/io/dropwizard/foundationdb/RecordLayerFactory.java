package io.dropwizard.foundationdb;

import com.apple.foundationdb.FDB;
import com.apple.foundationdb.record.logging.CompletionExceptionLogHelper;
import com.apple.foundationdb.record.provider.foundationdb.FDBDatabase;
import com.apple.foundationdb.record.provider.foundationdb.FDBDatabaseFactory;
import com.apple.foundationdb.record.provider.foundationdb.FDBReverseDirectoryCache;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.foundationdb.health.FoundationDBHealthCheck;
import io.dropwizard.foundationdb.instrumented.InstrumentedFDBDatabase;
import io.dropwizard.foundationdb.managed.RecordLayerManager;
import io.dropwizard.foundationdb.security.SecurityFactory;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Executor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class RecordLayerFactory {
    private static final Logger log = LoggerFactory.getLogger(RecordLayerFactory.class);

    @NotEmpty
    @JsonProperty
    private String name = "RecordLayer";
    @Min(100L)
    @JsonProperty
    private int apiVersion = 600;
    @NotEmpty
    @JsonProperty
    private String clusterFilePath;
    @Min(1L)
    @JsonProperty
    private int maxRetriableTransactionAttempts = 10;
    @NotNull
    @JsonProperty
    private Duration initialTransactionRetryDelay = Duration.milliseconds(10);
    @NotNull
    @JsonProperty
    private Duration maxTransactionRetryDelay = Duration.seconds(1);
    @Min(0L)
    @JsonProperty
    private int directoryCacheSize = 0;
    @NotNull
    @JsonProperty
    private Duration reverseDirectoryMaxTimePerTransaction =
            Duration.milliseconds(FDBReverseDirectoryCache.MAX_MILLIS_PER_TRANSACTION);
    @JsonProperty
    private int reverseDirectoryMaxRowsPerTransaction = FDBReverseDirectoryCache.MAX_ROWS_PER_TRANSACTION;
    @JsonProperty
    private String traceDirectory = null;
    @JsonProperty
    private String traceLogGroup = null;
    @NotEmpty
    @JsonProperty
    private String dataCenter;
    @Valid
    @JsonProperty
    private Optional<SecurityFactory> security = Optional.empty();
    @Min(0)
    @JsonProperty
    private int healthCheckRetries = 5;
    @NotNull
    @JsonProperty
    private Duration healthCheckTimeout = Duration.seconds(5);
    @NotNull
    @JsonProperty
    private String healthCheckSubspace = "health-checking";

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(final int apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getClusterFilePath() {
        return clusterFilePath;
    }

    public void setClusterFilePath(final String clusterFilePath) {
        this.clusterFilePath = clusterFilePath;
    }

    public int getMaxRetriableTransactionAttempts() {
        return maxRetriableTransactionAttempts;
    }

    public void setMaxRetriableTransactionAttempts(final int maxRetriableTransactionAttempts) {
        this.maxRetriableTransactionAttempts = maxRetriableTransactionAttempts;
    }

    public Duration getInitialTransactionRetryDelay() {
        return initialTransactionRetryDelay;
    }

    public void setInitialTransactionRetryDelay(final Duration initialTransactionRetryDelay) {
        this.initialTransactionRetryDelay = initialTransactionRetryDelay;
    }

    public Duration getMaxTransactionRetryDelay() {
        return maxTransactionRetryDelay;
    }

    public void setMaxTransactionRetryDelay(final Duration maxTransactionRetryDelay) {
        this.maxTransactionRetryDelay = maxTransactionRetryDelay;
    }

    public int getDirectoryCacheSize() {
        return directoryCacheSize;
    }

    public void setDirectoryCacheSize(final int directoryCacheSize) {
        this.directoryCacheSize = directoryCacheSize;
    }

    public String getTraceDirectory() {
        return traceDirectory;
    }

    public void setTraceDirectory(final String traceDirectory) {
        this.traceDirectory = traceDirectory;
    }

    public String getTraceLogGroup() {
        return traceLogGroup;
    }

    public void setTraceLogGroup(final String traceLogGroup) {
        this.traceLogGroup = traceLogGroup;
    }

    public Duration getReverseDirectoryMaxTimePerTransaction() {
        return reverseDirectoryMaxTimePerTransaction;
    }

    public void setReverseDirectoryMaxTimePerTransaction(final Duration reverseDirectoryMaxTimePerTransaction) {
        this.reverseDirectoryMaxTimePerTransaction = reverseDirectoryMaxTimePerTransaction;
    }

    public int getReverseDirectoryMaxRowsPerTransaction() {
        return reverseDirectoryMaxRowsPerTransaction;
    }

    public void setReverseDirectoryMaxRowsPerTransaction(final int reverseDirectoryMaxRowsPerTransaction) {
        this.reverseDirectoryMaxRowsPerTransaction = reverseDirectoryMaxRowsPerTransaction;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(final String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public Optional<SecurityFactory> getSecurity() {
        return security;
    }

    public void setSecurity(final Optional<SecurityFactory> security) {
        this.security = security;
    }

    public int getHealthCheckRetries() {
        return healthCheckRetries;
    }

    public void setHealthCheckRetries(final int healthCheckRetries) {
        this.healthCheckRetries = healthCheckRetries;
    }

    public Duration getHealthCheckTimeout() {
        return healthCheckTimeout;
    }

    public void setHealthCheckTimeout(final Duration healthCheckTimeout) {
        this.healthCheckTimeout = healthCheckTimeout;
    }

    public String getHealthCheckSubspace() {
        return healthCheckSubspace;
    }

    public void setHealthCheckSubspace(final String healthCheckSubspace) {
        this.healthCheckSubspace = healthCheckSubspace;
    }

    public FDBDatabase build(final MetricRegistry metrics,
                             final LifecycleEnvironment lifecycle,
                             final HealthCheckRegistry healthChecks) {
        return build(metrics, lifecycle, healthChecks, null, null);
    }

    public FDBDatabase build(final MetricRegistry metrics,
                             final LifecycleEnvironment lifecycle,
                             final HealthCheckRegistry healthChecks,
                             final Executor networkExecutor,
                             final Executor executor) {
        CompletionExceptionLogHelper.setAddSuppressed(false);
        final FDB fdb = FDB.selectAPIVersion(apiVersion);

        security.filter(SecurityFactory::isEnabled)
                .ifPresent(securityConf -> securityConf.addSecurityConfigurations(fdb.options()));

        final FDBDatabaseFactory factory = buildFDBDatabaseFactory(networkExecutor, executor);

        final String absoluteClusterFilePath = new File(clusterFilePath).getAbsolutePath();
        final FDBDatabase database = buildFDBDatabase(factory, absoluteClusterFilePath);

        final FDBDatabase instrumentedDatabase = instrumentFDBDatabase(factory, absoluteClusterFilePath, database, metrics);

        registerHealthCheck(healthChecks, database);

        manageFDBDatabase(lifecycle, database);

        log.info("Finished setting up record layer database={}", name);

        return instrumentedDatabase;
    }

    protected FDBDatabaseFactory buildFDBDatabaseFactory(final Executor networkExecutor, final Executor executor) {
        final FDBDatabaseFactory factory = FDBDatabaseFactory.instance();
        factory.setMaxAttempts(maxRetriableTransactionAttempts);
        factory.setInitialDelayMillis(initialTransactionRetryDelay.toMilliseconds());
        factory.setMaxDelayMillis(maxTransactionRetryDelay.toMilliseconds());
        factory.setDirectoryCacheSize(directoryCacheSize);
        factory.setReverseDirectoryMaxMillisPerTransaction(reverseDirectoryMaxTimePerTransaction.toMilliseconds());
        factory.setReverseDirectoryRowsPerTransaction(reverseDirectoryMaxRowsPerTransaction);
        factory.setTrace(traceDirectory, traceLogGroup);
        factory.setDatacenterId(dataCenter);

        if (networkExecutor != null) {
            factory.setNetworkExecutor(networkExecutor);
        }

        if (executor != null) {
            factory.setExecutor(executor);
        }

        return factory;
    }

    protected FDBDatabase buildFDBDatabase(final FDBDatabaseFactory factory, final String absoluteClusterFilePath) {
        return factory.getDatabase(absoluteClusterFilePath);
    }

    protected FDBDatabase instrumentFDBDatabase(final FDBDatabaseFactory factory, final String absoluteClusterFilePath,
                                                final FDBDatabase database, final MetricRegistry metrics) {
        return new InstrumentedFDBDatabase(factory, absoluteClusterFilePath, database, metrics, name);
    }

    protected void registerHealthCheck(final HealthCheckRegistry healthChecks, final FDBDatabase database) {
        final FoundationDBHealthCheck healthCheck = new FoundationDBHealthCheck(database.database(), name, healthCheckSubspace,
                healthCheckTimeout, healthCheckRetries);

        healthChecks.register(name, healthCheck);
    }

    protected void manageFDBDatabase(final LifecycleEnvironment lifecycle, final FDBDatabase database) {
        lifecycle.manage(new RecordLayerManager(database, name));
    }
}
