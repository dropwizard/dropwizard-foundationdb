package io.dropwizard.foundationdb.managed;

import com.apple.foundationdb.record.provider.foundationdb.FDBDatabase;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class RecordLayerManager implements Managed {
    private static final Logger log = LoggerFactory.getLogger(RecordLayerManager.class);

    private final FDBDatabase fdbDatabase;
    private final String name;

    public RecordLayerManager(final FDBDatabase fdbDatabase,
                              final String name) {
        this.fdbDatabase = requireNonNull(fdbDatabase);
        this.name = requireNonNull(name);
    }

    @Override
    public void start() throws Exception {
        log.info("RecordLayer {} starting", name);
    }

    @Override
    public void stop() throws Exception {
        log.info("RecordLayer {} shutting down", name);
        fdbDatabase.close();
    }
}
