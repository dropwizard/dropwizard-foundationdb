package io.dropwizard.foundationdb.health;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDBException;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FoundationDBHealthCheckTest {
    @Mock
    private Database mockedDatabase;
    @Mock
    private CompletableFuture<Object> mockedFuture;
    private FoundationDBHealthCheck healthCheck;

    @BeforeEach
    public void setUp() {
        healthCheck = new FoundationDBHealthCheck(mockedDatabase, "FoundationDB", "health-checking", Duration.seconds(5), 5);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnHealthyIfValidationSucceeds() {
        when(mockedDatabase.read(any())).thenReturn(mockedFuture);

        assertThat(healthCheck.check().isHealthy())
                .isTrue();

        verify(mockedDatabase).read(any(Function.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnUnhealthyIfValidationFails() {
        when(mockedDatabase.read(any(Function.class))).thenThrow(new FDBException("something bad", 1234));

        assertThat(healthCheck.check().isHealthy())
                .isFalse();

        verify(mockedDatabase).read(any(Function.class));
    }
}
