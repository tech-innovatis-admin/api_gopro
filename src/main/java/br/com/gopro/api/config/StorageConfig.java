package br.com.gopro.api.config;

import br.com.gopro.api.storage.NoopStorageService;
import br.com.gopro.api.storage.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DocumentsS3Properties.class)
public class StorageConfig {

    @Bean
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService storageService() {
        return new NoopStorageService();
    }
}
