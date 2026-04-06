package com.socialapp.application.post.usecase;

import com.socialapp.application.post.dto.request.PostRequestDtos.CreatePostRequest;
import com.socialapp.application.post.dto.response.PostResponseDtos.PostResponse;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.domain.file.entity.FileNode;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.post.valueobject.Privacy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class CreatePostUseCase {

    private final PostRepository postRepository;
    private final FileStorage    fileStorage;
    private final FileRepository fileRepository;

    public CreatePostUseCase(PostRepository postRepository,
                             FileStorage fileStorage,
                             FileRepository fileRepository) {
        this.postRepository = postRepository;
        this.fileStorage    = fileStorage;
        this.fileRepository = fileRepository;
    }

    @Transactional
    public PostResponse execute(String authorId,
                                CreatePostRequest request,
                                List<MultipartFile> files) {
        // 1. Upload files — lưu paths (object keys)
        List<String> filePaths = uploadFiles(files);

        // 2. Tạo Post (domain)
        Post post = Post.create(
                authorId,
                request.content(),
                Privacy.valueOf(request.privacy()),
                filePaths
        );
        postRepository.save(post);

        return toResponse(post, false);
    }

    private List<String> uploadFiles(List<MultipartFile> files) {
        List<String> paths = new ArrayList<>();
        if (files == null || files.isEmpty()) return paths;

        for (MultipartFile file : files) {
            String path = fileStorage.upload(file);
            fileRepository.save(FileNode.create(path,
                    file.getOriginalFilename(), file.getContentType()));
            paths.add(path);
        }
        return paths;
    }

    private PostResponse toResponse(Post post, boolean isLiked) {
        // ✅ FIX: Chuyển object paths → public URLs cho response
        List<String> fileUrls = post.getAttachedFilePaths() == null ? List.of()
                : post.getAttachedFilePaths().stream()
                        .map(fileStorage::getPublicUrl)
                        .toList();

        return new PostResponse(
                post.getId(), post.getAuthorId(), null, null,
                post.getContent(), post.getPrivacy().name(),
                post.getCounts().getLikeCount(),
                post.getCounts().getShareCount(),
                post.getCounts().getCommentCount(),
                isLiked, post.isShared(), post.getSharedFromPostId(),
                fileUrls,
                post.getCreatedAt(), post.getUpdatedAt()
        );
    }
}
