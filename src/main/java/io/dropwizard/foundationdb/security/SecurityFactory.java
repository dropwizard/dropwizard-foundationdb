package io.dropwizard.foundationdb.security;

import com.apple.foundationdb.NetworkOptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import javax.validation.constraints.NotEmpty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class SecurityFactory implements Discoverable {
    @NotEmpty
    @JsonProperty
    private String password;
    @JsonProperty
    private String verifyPeers;
    @NotEmpty
    @JsonProperty
    private String caFilePath = "/etc/ssl/certs/ca-bundle.crt";
    @JsonProperty
    private boolean enabled = true;

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getVerifyPeers() {
        return verifyPeers;
    }

    public void setVerifyPeers(final String verifyPeers) {
        this.verifyPeers = verifyPeers;
    }

    public String getCaFilePath() {
        return caFilePath;
    }

    public void setCaFilePath(final String caFilePath) {
        this.caFilePath = caFilePath;
    }

    public abstract void addSecurityConfigurations(NetworkOptions networkOptions);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
