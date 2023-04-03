package io.dropwizard.foundationdb.instrumented;

import com.apple.foundationdb.record.provider.foundationdb.FDBDatabase;
import com.apple.foundationdb.record.provider.foundationdb.FDBDatabaseFactory;
import com.apple.foundationdb.record.provider.foundationdb.FDBRecordContext;
import com.apple.foundationdb.record.provider.foundationdb.FDBStoreTimer;

import com.codahale.metrics.MetricRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A {@link MetricRegistry} instrumented version of the RecDB {@link FDBDatabase}.
 */
public class InstrumentedFDBDatabase extends FDBDatabase {
    private static final Logger log = LoggerFactory.getLogger(InstrumentedFDBDatabase.class);

    private final MetricRegistry metrics;
    private final FDBDatabase database;
    private final String name;

    public InstrumentedFDBDatabase(final FDBDatabaseFactory factory,
                                   @Nullable final String clusterFile,
                                   final FDBDatabase database,
                                   final MetricRegistry metrics,
                                   final String name) {
        super(factory, clusterFile);

        this.database = requireNonNull(database);
        this.metrics = requireNonNull(metrics);
        this.name = requireNonNull(name);
    }

    @Override
    public <T> T run(@Nullable final FDBStoreTimer timer,
                     @Nullable final Map<String, String> mdcContext,
                     final Function<? super FDBRecordContext, ? extends T> retriable) {
        if (timer != null) {
            return database.run(new InstrumentedFDBStoreTimer(timer, metrics, name), mdcContext, retriable);
        }
        return database.run(null, mdcContext, retriable);
    }

    @Override
    public <T> CompletableFuture<T> runAsync(@Nullable final FDBStoreTimer timer,
                                             @Nullable final Map<String, String> mdcContext,
                                             final Function<? super FDBRecordContext, CompletableFuture<? extends T>> retriable) {
        if (timer != null) {
            return database.runAsync(new InstrumentedFDBStoreTimer(timer, metrics, name), mdcContext, retriable);
        }
        return database.runAsync(null, mdcContext, retriable);
    }

    @Override
    public <T> T asyncToSync(final FDBStoreTimer timer, final FDBStoreTimer.Wait event, final CompletableFuture<T> async) {
        if (timer != null) {
            return database.asyncToSync(new InstrumentedFDBStoreTimer(timer, metrics, name), event, async);
        }
        return database.asyncToSync(null, event, async);
    }

    @Override
    public <T> CompletableFuture<T> runAsync(final Function<? super FDBRecordContext, CompletableFuture<? extends T>> retriable) {
        return runAsync(null, null, retriable);
    }
}
