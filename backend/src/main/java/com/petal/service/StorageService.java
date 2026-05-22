package com.petal.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String storeOrderPhoto(MultipartFile file, String prefix);

    String storeFloristLogo(MultipartFile file);

    String resolveUrl(String storedPath);
}
