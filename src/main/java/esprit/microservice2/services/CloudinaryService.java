package esprit.microservice2.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    /**
     * Upload a medical record image to Cloudinary
     */
    public String uploadImage(MultipartFile file, Long recordId) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "medical-records/" + recordId,
                            "resource_type", "auto",
                            "overwrite", true,
                            "quality", "auto",
                            "fetch_format", "auto"));

            String url = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully for medical record: {}, URL: {}", recordId, url);
            return url;

        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a medical record image from Cloudinary
     */
    public void deleteImage(Long recordId) {
        try {
            cloudinary.uploader().destroy(
                    "medical-records/" + recordId,
                    ObjectUtils.emptyMap());
            log.info("Image deleted for medical record: {}", recordId);
        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage());
        }
    }
}
