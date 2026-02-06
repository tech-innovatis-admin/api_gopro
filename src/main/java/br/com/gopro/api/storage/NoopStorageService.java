package br.com.gopro.api.storage;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

public class NoopStorageService implements StorageService {

    @Override
    public void uploadFile(String bucket, String key, InputStream inputStream, long sizeBytes, String contentType) {
        throw new IllegalStateException("S3 nao configurado. Defina app.documents.s3.bucket para habilitar uploads.");
    }

    @Override
    public void deleteFile(String bucket, String key) {
        throw new IllegalStateException("S3 nao configurado. Defina app.documents.s3.bucket para habilitar delecao.");
    }

    @Override
    public boolean fileExists(String bucket, String key) {
        throw new IllegalStateException("S3 nao configurado. Defina app.documents.s3.bucket para habilitar consulta.");
    }

    @Override
    public URI generatePresignedDownloadUrl(String bucket, String key, Duration expiresIn) {
        throw new IllegalStateException("S3 nao configurado. Defina app.documents.s3.bucket para habilitar download.");
    }
}
