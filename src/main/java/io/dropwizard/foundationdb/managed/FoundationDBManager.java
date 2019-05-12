package io.dropwizard.foundationdb.managed;

import com.apple.foundationdb.FDB;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Manages FDB with the application lifecycle.
 */
public class FoundationDBManager implements Managed {
    private final Logger log = LoggerFactory.getLogger(FoundationDBManager.class);

    private final FDB fdb;
    private final String name;

    public FoundationDBManager(final FDB fdb,
                               final String name) {
        this.fdb = requireNonNull(fdb);
        this.name = requireNonNull(name);
    }

    @Override
    public void start() throws Exception {
        log.info("FoundationDB {} starting", name);
    }

    @Override
    public void stop() throws Exception {
        log.info("FoundationDB {} shutting down", name);
        fdb.stopNetwork();
    }
}
