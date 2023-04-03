package io.dropwizard.foundationdb;

import com.apple.foundationdb.record.provider.foundationdb.FDBDatabase;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

public abstract class RecordLayerBundle<T extends Configuration> implements ConfiguredBundle<T> {
    @Nullable
    private FDBDatabase database;

    @Nullable
    private final Executor networkExecutor;
    @Nullable
    private final Executor executor;

    protected RecordLayerBundle() {
        this(null, null);
    }

    protected RecordLayerBundle(@Nullable final Executor networkExecutor,
                                @Nullable final Executor executor) {
        this.networkExecutor = networkExecutor;
        this.executor = executor;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        // do nothing
    }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception {
        final RecordLayerFactory recordLayerFactory = requireNonNull(getRecordLayerFactory(configuration));

        this.database = Objects.requireNonNull(recordLayerFactory.build(environment.metrics(), environment.lifecycle(),
                environment.healthChecks(), networkExecutor, executor));
    }

    public abstract RecordLayerFactory getRecordLayerFactory(T configuration);

    public FDBDatabase getDatabase() {
        return requireNonNull(database);
    }
}
