package io.dropwizard.foundationdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.Test;

import java.io.File;

import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordLayerFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<RecordLayerFactory> factory =
            new YamlConfigurationFactory<>(RecordLayerFactory.class, validator, objectMapper, "dw");

    @Test
    public void shouldBuildAFoundationDBFactory() throws Exception {
        final File yml = new File(Resources.getResource("yml/record-layer.yml").toURI());
        final RecordLayerFactory fdbFactory = factory.build(yml);
        assertThat(fdbFactory.getClusterFilePath())
                .isEqualTo("src/test/resources/fdb_dev.cluster");
        assertThat(fdbFactory.getInitialTransactionRetryDelay())
                .isEqualTo(Duration.milliseconds(5));
    }
}
