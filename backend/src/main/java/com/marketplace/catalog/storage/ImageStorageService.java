package com.marketplace.catalog.storage;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    String storeProductImage(String sellerId, String productId, MultipartFile file) throws IOException;

    void delete(String url) throws IOException;
}
