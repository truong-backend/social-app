package com.socialapp.application.shared.port;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorage {

    /** Upload file lên storage, trả về object path (key). */
    String upload(MultipartFile file);

    /** Xóa file theo path (object key). */
    void delete(String path);

    /** Xóa nhiều file. */
    void deleteAll(List<String> paths);

    /**
     * Chuyển đổi object path (key) thành public HTTP URL có thể truy cập từ browser.
     * Ví dụ: "uuid_avatar.jpg" → "http://localhost:9000/socialapp/uuid_avatar.jpg"
     */
    String getPublicUrl(String path);
}
