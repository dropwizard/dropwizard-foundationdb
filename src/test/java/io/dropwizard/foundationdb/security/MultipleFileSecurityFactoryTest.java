package io.dropwizard.foundationdb.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import java.io.File;

import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

public class MultipleFileSecurityFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<SecurityFactory> factory =
            new YamlConfigurationFactory<>(SecurityFactory.class, validator, objectMapper, "dw");

    @Test
    public void shouldBuildASecurityFactory() throws Exception {
        final File yml = new File(Resources.getResource("yml/security.yml").toURI());
        final SecurityFactory securityFactory = factory.build(yml);
        assertThat(securityFactory)
                .isInstanceOf(MultipleFileSecurityFactory.class);
        final MultipleFileSecurityFactory multipleFileSecurityFactory = (MultipleFileSecurityFactory) securityFactory;
        assertThat(multipleFileSecurityFactory.getKeyFilePath())
                .isEqualTo("/path/to/file");
        assertThat(multipleFileSecurityFactory.getCertificateChainFilePath())
                .isEqualTo("/path/to/file");
        assertThat(multipleFileSecurityFactory.getCaFilePath())
                .isNotNull();
        assertThat(multipleFileSecurityFactory.getVerifyPeers())
                .isNotNull();
        assertThat(multipleFileSecurityFactory.getPassword())
                .isEqualTo("secret");
    }
}
