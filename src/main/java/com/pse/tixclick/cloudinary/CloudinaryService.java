package com.pse.tixclick.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    Cloudinary cloudinary;

    public String uploadImageToCloudinary(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
        return uploadResult.get("url").toString();
    }

    public String uploadDocumentToCloudinary(MultipartFile file) throws IOException {
        String fileType = file.getContentType();

        // Kiểm tra định dạng file hợp lệ (PDF, DOC, DOCX, TXT)
        if (fileType == null || !fileType.matches("application/pdf|application/msword|application/vnd.openxmlformats-officedocument.wordprocessingml.document|text/plain")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        // Cấu hình upload với `resource_type: raw`
        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", "auto");
        String folderName = "namphan";
        // Upload file lên Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folderName,
                        "access_mode", "public" // Đảm bảo file có thể được truy cập công khai

                ));

        // Lấy URL file đã upload
        return uploadResult.get("secure_url").toString();
    }



}
