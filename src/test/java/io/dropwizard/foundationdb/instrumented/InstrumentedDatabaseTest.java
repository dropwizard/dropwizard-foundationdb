package io.dropwizard.foundationdb.instrumented;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.tuple.Tuple;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstrumentedDatabaseTest {
    private static final String NAME = "FoundationDB";

    @Mock
    private Database database;

    @Test
    public void shouldRecordMetricsForInstrumentedDatabase() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final InstrumentedDatabase instrumentedDatabase = new InstrumentedDatabase(database, metricRegistry, NAME);

        instrumentedDatabase.run((transaction) -> {
            transaction.set(Tuple.from("hello").pack(), Tuple.from("world").pack());
            return null;
        });

        assertThat(metricRegistry.timer(MetricRegistry.name(NAME, "run.timeInNanos")).getCount())
                .isEqualTo(1L);
    }

    @Test
    public void shouldRecordMetricsForInstrumentedDatabaseAsyncCalls() throws InterruptedException, ExecutionException {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final InstrumentedDatabase instrumentedDatabase = new InstrumentedDatabase(database, metricRegistry, NAME);
        final CompletableFuture<Object> testFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
            return null;
        });

        when(database.runAsync(any(), any())).thenReturn(testFuture);

        instrumentedDatabase.runAsync((transaction) -> {
            transaction.set(Tuple.from("hello").pack(), Tuple.from("world").pack());
            return CompletableFuture.completedFuture(null);
        }).get();

        final String runAsyncTimerName = MetricRegistry.name(NAME, "runAsync.timeInNanos");
        assertThat(metricRegistry.timer(runAsyncTimerName).getCount())
                .isEqualTo(1L);
        assertThat(metricRegistry.timer(runAsyncTimerName).getSnapshot().getMax())
                .isGreaterThan(50L);
    }
}
