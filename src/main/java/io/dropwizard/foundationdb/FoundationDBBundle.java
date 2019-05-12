package io.dropwizard.foundationdb;

import com.apple.foundationdb.Database;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

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
