package io.dropwizard.foundationdb.instrumented;

import com.apple.foundationdb.record.provider.foundationdb.FDBStoreTimer;

import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * A {@link MetricRegistry} instrumented version of the {@link FDBStoreTimer}.
 * All calls record will also record a time event in the instance metric registry.
 */
public class InstrumentedFDBStoreTimer extends FDBStoreTimer {

    private final FDBStoreTimer timer;
    private final MetricRegistry metrics;
    private final String name;

    public InstrumentedFDBStoreTimer(final FDBStoreTimer timer, 
                                     final MetricRegistry metrics,
                                     final String name) {
        this.timer = requireNonNull(timer);
        this.metrics = requireNonNull(metrics);
        this.name = requireNonNull(name);
    }

    @Override
    public void record(final Event event, final long timeDifferenceNanos) {
        final String metricName = MetricRegistry.name(name, event.name());
        metrics.timer(metricName).update(timeDifferenceNanos, TimeUnit.NANOSECONDS);
        timer.record(event, timeDifferenceNanos);
    }
}
