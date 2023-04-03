package io.dropwizard.foundationdb;

import com.apple.foundationdb.Database;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.Executor;


import static java.util.Objects.requireNonNull;

public abstract class FoundationDBBundle<T extends Configuration> implements ConfiguredBundle<T> {
    @Nullable
    private Database database;

    @Nullable
    private final Executor executor;

    protected FoundationDBBundle() {
        this(null);
    }

    protected FoundationDBBundle(@Nullable final Executor executor) {
        this.executor = executor;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        // do nothing
    }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception {
        final FoundationDBFactory foundationDBFactory = requireNonNull(getFoundationDBFactory(configuration));

        this.database = requireNonNull(foundationDBFactory.build(environment.metrics(), environment.lifecycle(),
                environment.healthChecks(), executor));
    }

    public abstract FoundationDBFactory getFoundationDBFactory(T configuration);

    public Database getDatabase() {
        return requireNonNull(database);
    }
}
