package com.marketplace.catalog.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalImageStorageService implements ImageStorageService {

    private final Path rootDir;
    private final String publicBaseUrl;

    public LocalImageStorageService(
            @Value("${app.storage.local-dir:uploads}") String localDir,
            @Value("${app.storage.public-base-url:http://localhost:8080/api/v1/media}") String publicBaseUrl
    ) throws IOException {
        this.rootDir = Path.of(localDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        Files.createDirectories(this.rootDir);
    }

    @Override
    public String storeProductImage(String sellerId, String productId, MultipartFile file) throws IOException {
        String ext = extension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Path target = rootDir.resolve(sellerId).resolve(productId).resolve(filename);
        Files.createDirectories(target.getParent());
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return publicBaseUrl + "/" + sellerId + "/" + productId + "/" + filename;
    }

    @Override
    public void delete(String url) throws IOException {
        if (!url.startsWith(publicBaseUrl)) {
            return;
        }
        String relative = url.substring(publicBaseUrl.length() + 1);
        Path path = rootDir.resolve(relative).normalize();
        if (path.startsWith(rootDir)) {
            Files.deleteIfExists(path);
        }
    }

    public Path getRootDir() {
        return rootDir;
    }

    public Path resolve(String sellerId, String productId, String filename) {
        return rootDir.resolve(sellerId).resolve(productId).resolve(filename).normalize();
    }

    private static String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
