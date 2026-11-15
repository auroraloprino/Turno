package com.turno.common.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("turno.sicurezza")
public record SicurezzaProperties(List<String> percorsiPubblici) {

    public SicurezzaProperties {
        percorsiPubblici = percorsiPubblici == null ? List.of() : percorsiPubblici;
    }
}
