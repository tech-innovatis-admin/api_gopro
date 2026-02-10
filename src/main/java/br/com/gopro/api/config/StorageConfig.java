package br.com.gopro.api.config;

import br.com.gopro.api.storage.LocalFileStorageService;
import br.com.gopro.api.storage.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(DocumentsS3Properties.class)
public class StorageConfig {

    @Bean
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService storageService(
            @Value("${app.documents.local.base-path:./storage/documents}") String localBasePath
    ) {
        return new LocalFileStorageService(Path.of(localBasePath));
    }
}
