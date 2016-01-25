package com.zemiak.nasphotos.lookup;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

@Singleton
@Startup
public class ConfigurationProvider {
    private final Map<String, String> configuration = new HashMap<>();

    @PostConstruct
    public void readConfiguration() {
	ResourceBundle props = ResourceBundle.getBundle("config");

        props.keySet().stream().forEach((key) -> {
            configuration.put(key, props.getString(key));
        });
    }

    @Produces
    public String getString(InjectionPoint point) {
        String fieldName = point.getMember().getName();
        String valueForFieldName = configuration.get(fieldName);
        return valueForFieldName;
    }

    @Produces
    public Integer getInt(InjectionPoint point) {
        String stringValue = getString(point);
        if (stringValue == null) {
            return null;
        }

        return Integer.parseInt(stringValue);
    }

    @Produces
    public Boolean getBoolean(InjectionPoint point) {
        String stringValue = getString(point);
        if (stringValue == null) {
            return null;
        }

        return "true".equals(stringValue);
    }

    public String getConfigValue(String key) {
        return configuration.get(key);
    }
}
