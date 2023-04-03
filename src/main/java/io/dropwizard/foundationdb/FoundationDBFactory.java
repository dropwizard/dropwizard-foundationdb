package io.dropwizard.foundationdb;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.foundationdb.health.FoundationDBHealthCheck;
import io.dropwizard.foundationdb.instrumented.InstrumentedDatabase;
import io.dropwizard.foundationdb.managed.FoundationDBManager;
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

/**
 * Factory used to build a 'vanilla' Foundation DB database instance.
 */
public class FoundationDBFactory {
    private static final Logger log = LoggerFactory.getLogger(FoundationDBFactory.class);

    @NotEmpty
    @JsonProperty
    private String name = "FoundationDB";
    @Min(100L)
    @JsonProperty
    private int apiVersion = 600;
    @NotEmpty
    @JsonProperty
    private String clusterFilePath;
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

    public Database build(final MetricRegistry metrics,
                          final LifecycleEnvironment lifecycle,
                          final HealthCheckRegistry healthChecks) {
        return build(metrics, lifecycle, healthChecks, FDB.DEFAULT_EXECUTOR);
    }

    public Database build(final MetricRegistry metrics,
                          final LifecycleEnvironment lifecycle,
                          final HealthCheckRegistry healthChecks,
                          final Executor executor) {
        final Executor actualExecutor = executor != null ? executor : FDB.DEFAULT_EXECUTOR;

        final String absoluteClusterFilePath = new File(clusterFilePath).getAbsolutePath();

        final FDB fdb = FDB.selectAPIVersion(apiVersion);

        security.filter(SecurityFactory::isEnabled)
                .ifPresent(securityConf -> securityConf.addSecurityConfigurations(fdb.options()));

        final Database database = buildDatabase(fdb, absoluteClusterFilePath, actualExecutor);

        final Database instrumentedDatabase = instrumentDatabase(database, metrics);

        instrumentedDatabase.options().setDatacenterId(dataCenter);

        registerHealthCheck(healthChecks, database);

        manageDatabase(lifecycle, fdb);

        log.info("Finished setting up fdbDatabase={}", name);

        return instrumentedDatabase;
    }

    protected Database buildDatabase(final FDB fdb, final String absoluteClusterFilePath, final Executor executor) {
        return fdb.open(absoluteClusterFilePath, executor);
    }

    protected Database instrumentDatabase(final Database database, final MetricRegistry metrics) {
        return new InstrumentedDatabase(database, metrics, name);
    }

    protected void registerHealthCheck(final HealthCheckRegistry healthChecks, final Database database) {
        final FoundationDBHealthCheck healthCheck = new FoundationDBHealthCheck(database, name, healthCheckSubspace, healthCheckTimeout,
                healthCheckRetries);

        healthChecks.register(name, healthCheck);
    }

    protected void manageDatabase(final LifecycleEnvironment lifecycle, final FDB fdb) {
        lifecycle.manage(new FoundationDBManager(fdb, name));
    }
}
