package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.FileResponse;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.neo4j.FileRepository;
import com.stu.socialnetworkapi.repository.neo4j.UserRepository;
import com.stu.socialnetworkapi.service.itf.FileService;
import com.stu.socialnetworkapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final JwtUtil jwtUtil;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final MinioStorageService minioStorageService;

    // ------------------------------------------------------------------ load

    @Override
    public FileResponse load(String id) {
        File fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.FILE_NOT_FOUND));

        if (!minioStorageService.objectExists(id)) {
            throw new ApiException(ErrorCode.FILE_NOT_FOUND);
        }

        return FileResponse.builder()
                .name(fileEntity.getName())
                .contentType(fileEntity.getContentType())
                .resource(new InputStreamResource(minioStorageService.getObject(id)))
                .build();
    }

    // ----------------------------------------------------------------- upload

    @Override
    public File upload(MultipartFile file) {
        if (file.isEmpty()) throw new ApiException(ErrorCode.FILE_REQUIRED);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String objectName = UUID.randomUUID() + extension;

        minioStorageService.uploadAsync(file, objectName);

        File newFile = File.builder()
                .id(objectName)
                .name(originalFilename)
                .contentType(file.getContentType())
                .uploader(getCurrentUserRequiredAuthentication())
                .build();

        return fileRepository.save(newFile);
    }

    @Override
    public List<File> upload(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ApiException(ErrorCode.FILE_REQUIRED);
        }

        List<File> uploadedFiles = new ArrayList<>();
        User uploader = getCurrentUserRequiredAuthentication();

        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) throw new ApiException(ErrorCode.FILE_REQUIRED);

                String originalFilename = file.getOriginalFilename();
                String extension = getFileExtension(originalFilename);
                String objectName = UUID.randomUUID() + extension;

                minioStorageService.uploadAsync(file, objectName);

                File newFile = File.builder()
                        .id(objectName)
                        .name(originalFilename)
                        .contentType(file.getContentType())
                        .uploader(uploader)
                        .build();
                uploadedFiles.add(newFile);
            }

            return fileRepository.saveAll(uploadedFiles);
        } catch (Exception e) {
            rollBackUploadFilesFailed(uploadedFiles);
            if (e instanceof ApiException exception) throw exception;
            throw new ApiException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    // ----------------------------------------------------------------- delete

    @Override
    public void deleteFileById(String id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.FILE_NOT_FOUND));
        deleteFile(file);
    }

    @Override
    public void deleteFile(File file) {
        minioStorageService.deleteObject(file.getId());
        fileRepository.delete(file);
    }

    @Override
    public void deleteFiles(List<File> files) {
        files.forEach(this::deleteFile);
    }

    @Override
    public void deleteFilesById(List<String> ids) {
        List<File> files = fileRepository.findAllById(ids);
        deleteFiles(files);
    }

    // --------------------------------------------------------------- helpers

    private String getFileExtension(String filename) {
        int lastDotIndex = Objects.requireNonNull(filename).lastIndexOf(".");
        if (lastDotIndex == -1) return "";
        return filename.substring(lastDotIndex);
    }

    private void rollBackUploadFilesFailed(List<File> uploadedFiles) {
        for (File uploadedFile : uploadedFiles) {
            try {
                minioStorageService.deleteObject(uploadedFile.getId());
                fileRepository.delete(uploadedFile);
            } catch (Exception deleteEx) {
                log.error("Failed to delete file during rollback: {}", uploadedFile.getId());
            }
        }
    }

    private User getCurrentUserRequiredAuthentication() {
        return userRepository.findById(jwtUtil.getUserIdRequiredAuthentication())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }
}