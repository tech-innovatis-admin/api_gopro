package br.com.gopro.api.storage;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

public interface StorageService {
    void uploadFile(String bucket, String key, InputStream inputStream, long sizeBytes, String contentType);
    void deleteFile(String bucket, String key);
    boolean fileExists(String bucket, String key);
    URI generatePresignedDownloadUrl(String bucket, String key, Duration expiresIn);
}
