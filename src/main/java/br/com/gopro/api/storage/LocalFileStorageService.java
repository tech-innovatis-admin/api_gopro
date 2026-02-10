package br.com.gopro.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public class LocalFileStorageService implements StorageService {

    private final Path basePath;

    public LocalFileStorageService(Path basePath) {
        this.basePath = basePath.toAbsolutePath().normalize();
    }

    @Override
    public void uploadFile(String bucket, String key, InputStream inputStream, long sizeBytes, String contentType) {
        Path target = resolvePath(bucket, key);
        Path parent = target.getParent();

        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao gravar arquivo no storage local", ex);
        }
    }

    @Override
    public void deleteFile(String bucket, String key) {
        Path target = resolvePath(bucket, key);
        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao remover arquivo do storage local", ex);
        }
    }

    @Override
    public boolean fileExists(String bucket, String key) {
        return Files.exists(resolvePath(bucket, key));
    }

    @Override
    public URI generatePresignedDownloadUrl(String bucket, String key, Duration expiresIn) {
        return resolvePath(bucket, key).toUri();
    }

    private Path resolvePath(String bucket, String key) {
        String normalizedBucket = (bucket == null || bucket.isBlank()) ? "local-documents" : bucket;
        String normalizedKey = (key == null || key.isBlank()) ? "file" : key;

        Path bucketRoot = basePath.resolve(normalizedBucket).normalize();
        Path resolved = bucketRoot.resolve(normalizedKey).normalize();

        if (!resolved.startsWith(bucketRoot)) {
            throw new IllegalArgumentException("Caminho de arquivo invalido");
        }

        return resolved;
    }
}

