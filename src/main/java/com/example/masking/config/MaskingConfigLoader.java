package com.example.masking.config;

import com.example.masking.model.MaskingConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class MaskingConfigLoader {

    @Value("${masking.config.file:masking-config.yaml}")
    private String configFile;

    @Bean
    public MaskingConfig loadMaskingConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ClassPathResource resource = new ClassPathResource(configFile);

        try (InputStream inputStream = resource.getInputStream()) {
            MaskingConfigWrapper wrapper = mapper.readValue(inputStream, MaskingConfigWrapper.class);
            return wrapper.getMasking();
        }
    }

    private static class MaskingConfigWrapper {
        private MaskingConfig masking;

        public MaskingConfig getMasking() {
            return masking;
        }

        public void setMasking(MaskingConfig masking) {
            this.masking = masking;
        }
    }
}
