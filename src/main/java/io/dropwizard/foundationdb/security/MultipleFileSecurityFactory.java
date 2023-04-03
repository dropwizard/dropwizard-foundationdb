package io.dropwizard.foundationdb.security;

import com.apple.foundationdb.NetworkOptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonTypeName("multi-file")
public class MultipleFileSecurityFactory extends SecurityFactory {
    @NotNull
    @JsonProperty
    private String certificateChainFilePath;

    @NotEmpty
    @JsonProperty
    private String keyFilePath;

    public String getCertificateChainFilePath() {
        return certificateChainFilePath;
    }

    public void setCertificateChainFilePath(final String certificateChainFilePath) {
        this.certificateChainFilePath = certificateChainFilePath;
    }

    public String getKeyFilePath() {
        return keyFilePath;
    }

    public void setKeyFilePath(final String keyFilePath) {
        this.keyFilePath = keyFilePath;
    }

    @Override
    public void addSecurityConfigurations(final NetworkOptions networkOptions) {
        networkOptions.setTLSPassword(getPassword());
        networkOptions.setTLSCaPath(getCaFilePath());
        if (getVerifyPeers() != null) {
            networkOptions.setTLSVerifyPeers(getVerifyPeers().getBytes());
        }
        networkOptions.setTLSCertPath(getCertificateChainFilePath());
        networkOptions.setTLSKeyPath(getKeyFilePath());
    }
}
