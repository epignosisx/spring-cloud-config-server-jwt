package com.epignosisx.configserver.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private String[] secrets;

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
        if (StringUtils.isEmpty(secret)) {
            this.secrets = new String[0];
        } else {
            this.secrets = secret.trim().split("\\s*,\\s*");
        }
    }

    public String[] getSecrets() { return this.secrets; }
}

