package ru.petrelevich.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class ConfigProviderYaml implements ConfigProvider {
    private final String fileName;
    private final ObjectMapper objectMapper;

    public ConfigProviderYaml(String fileName, ObjectMapper objectMapper) {
        this.fileName = fileName;
        this.objectMapper = objectMapper;
    }

    @Override
    public Config get() {
        try {
            return objectMapper.readValue(new File(fileName), Config.class);
        } catch (Exception ex) {
            throw new ConfigException("can't read config file:" + fileName, ex);
        }
    }
}
