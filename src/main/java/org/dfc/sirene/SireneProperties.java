package org.dfc.sirene;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("sirene")
public record SireneProperties(String datasetsDir, String descriptorsDir, List<Entry> datasets) {
    public record Entry(String dataset, String descriptor, String table) {}
}
