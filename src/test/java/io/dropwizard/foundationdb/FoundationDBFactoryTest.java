package io.dropwizard.foundationdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import java.io.File;

import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

public class FoundationDBFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<FoundationDBFactory> factory =
            new YamlConfigurationFactory<>(FoundationDBFactory.class, validator, objectMapper, "dw");

    @Test
    public void shouldBuildAFoundationDBFactory() throws Exception {
        final File yml = new File(Resources.getResource("yml/foundationdb.yml").toURI());
        final FoundationDBFactory fdbFactory = factory.build(yml);
        assertThat(fdbFactory.getApiVersion())
                .isEqualTo(600);
        assertThat(fdbFactory.getClusterFilePath())
                .isEqualTo("src/test/resources/fdb_dev.cluster");
    }
}
