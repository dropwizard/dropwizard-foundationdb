package io.dropwizard.foundationdb.health;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDBException;
import com.apple.foundationdb.tuple.Tuple;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

import javax.validation.constraints.Min;

/**
 * Performs health checks against a FoundationDB cluster by doing a simple set and clear in a single transaction.
 */
public class FoundationDBHealthCheck extends HealthCheck {
    private static final Logger log = LoggerFactory.getLogger(FoundationDBHealthCheck.class);

    private final Database database;
    private final String name;
    private final String subspacePath;
    private final Duration timeout;
    @Min(0)
    private final int retries;

    public FoundationDBHealthCheck(final Database database,
                                   final String name,
                                   final String subspacePath,
                                   final Duration timeout,
                                   @Min(0) final int retries) {
        this.database = Objects.requireNonNull(database);
        this.name = Objects.requireNonNull(name);
        this.subspacePath = Objects.requireNonNull(subspacePath);
        this.timeout = Objects.requireNonNull(timeout);
        this.retries = retries;
    }

    @Override
    protected Result check() {
        try {
            final String key = UUID.randomUUID().toString();

            // read an arbitrary key that likely doesn't exist
            final byte[] result = database.read((transaction -> {
                transaction.options().setTimeout(timeout.toMilliseconds());
                transaction.options().setRetryLimit(retries);
                return transaction.get(Tuple.from(subspacePath, key).pack());
            })).join();

            log.debug("Health check against FoundationDB successful for database={} result={}", name, result);
            return Result.healthy();
        } catch (final FDBException e) {
            log.warn("FDB error caused health check to fail for database={} with error code={}", name, e.getCode(), e);
            return Result.unhealthy("Exception causing health check failure", e);
        } catch (final Exception e) {
            log.warn("Unable to perform a health check against database={}", name, e);
            return Result.unhealthy("Exception causing health check failure", e);
        }
    }
}
