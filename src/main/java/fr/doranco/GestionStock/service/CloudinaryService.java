package fr.doranco.GestionStock.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;
    public static final String FOLDER_PRODUCTS = "stockiflow/products";
    public static final String FOLDER_PROFILES  = "stockiflow/profiles";

    public String uploadImage(MultipartFile file, String folder) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Map<String, Object> options = new HashMap<>();
        options.put("folder", folder);
        options.put("overwrite", true);

        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
        String url = (String) result.get("secure_url");
        log.info("[CLOUDINARY] Image uploaded to {}: {}", folder, url);
        return url;
    }

    public void deleteImage(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.isBlank()) return;
        String publicId = extractPublicId(imageUrl);
        Map<String, Object> options = new HashMap<>();
        cloudinary.uploader().destroy(publicId, options);
        log.info("[CLOUDINARY] Image deleted: {}", publicId);
    }

    private String extractPublicId(String imageUrl) {
        String[] parts = imageUrl.split("/upload/");
        String afterUpload = parts[1];
        return afterUpload.replaceFirst("v\\d+/", "").replaceAll("\\.[^.]+$", "");
    }
}