package com.socialapp.infrastructure.adapter.persistence;

import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.entity.Comment;
import com.socialapp.domain.model.entity.FileEntity;
import com.socialapp.domain.model.entity.Keyword;
import com.socialapp.domain.model.valueobject.*;
import com.socialapp.domain.repository.PostRepository;
import com.socialapp.infrastructure.adapter.persistence.neo4j.node.*;
import com.socialapp.infrastructure.adapter.persistence.neo4j.repository.PostNeo4jRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PostRepositoryAdapter implements PostRepository {

    private final PostNeo4jRepository postRepo;

    public PostRepositoryAdapter(PostNeo4jRepository postRepo) {
        this.postRepo = postRepo;
    }

    // ── Domain → Node ────────────────────────────────────────────────────

    private PostNode toNode(Post post) {
        PostNode node = new PostNode(
                post.getId(),
                post.getContent().getValue(),
                post.getPrivacy().name(),
                post.getLikeCount(),
                post.getShareCount(),
                post.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getDeletedAt()
        );

        // @Relationship ATTACH_FILES
        List<FileNode> fileNodes = post.getAttachments().stream()
                .map(f -> new FileNode(
                        f.getMeta().getPath(), f.getMeta().getName(),
                        f.getMeta().getContentType(), f.getMeta().getSizeBytes()))
                .toList();
        node.setAttachments(fileNodes);

        // @Relationship HAS_COMMMENT
        List<CommentNode> commentNodes = post.getComments().stream()
                .map(this::toCommentNode)
                .toList();
        node.setComments(commentNodes);

        // @Relationship HAS_KEYWORDS
        List<KeywordNode> kwNodes = post.getKeywords().stream()
                .map(k -> new KeywordNode(k.getText()))
                .toList();
        node.setKeywords(kwNodes);

        // @Relationship SHARED
        if (post.getSharedFromPostId() != null) {
            PostNode sharedFromStub = new PostNode();
            sharedFromStub.setId(post.getSharedFromPostId());
            node.setSharedFrom(sharedFromStub);
        }

        return node;
    }

    private CommentNode toCommentNode(Comment c) {
        CommentNode cn = new CommentNode(
                c.getId(),
                c.getContent().getValue(),
                c.getLikeCount(),
                c.getReplyCount(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );

        // @Relationship ATTACH_FILE
        if (c.getAttachedFile() != null) {
            FileMeta meta = c.getAttachedFile().getMeta();
            cn.setAttachedFile(new FileNode(
                    meta.getPath(), meta.getName(),
                    meta.getContentType(), meta.getSizeBytes()));
        }

        // @Relationship REPLIED
        List<CommentNode> replyNodes = c.getReplies().stream()
                .map(this::toCommentNode).toList();
        cn.setReplies(replyNodes);

        return cn;
    }

    // ── Node → Domain ────────────────────────────────────────────────────

    private Post toDomain(PostNode node) {
        Post post = new Post(
                node.getId(),
                null, // authorId không lưu trên PostNode; set bởi caller nếu cần
                new PostContent(node.getContent() != null ? node.getContent() : ""),
                PostPrivacy.valueOf(node.getPrivacy())
        );
        post.setLikeCount(node.getLikeCount());
        post.setShareCount(node.getShareCount());
        post.setCommentCount(node.getCommentCount());

        // @Relationship SHARED
        if (node.getSharedFrom() != null) {
            post.setSharedFromPostId(node.getSharedFrom().getId());
        }

        // @Relationship ATTACH_FILES
        if (node.getAttachments() != null) {
            node.getAttachments().forEach(fn ->
                    post.attachFile(new FileEntity(
                            new FileMeta(fn.getPath(), fn.getName(),
                                    fn.getContentType(), fn.getSizeBytes())))
            );
        }

        // @Relationship HAS_COMMMENT
        if (node.getComments() != null) {
            node.getComments().stream()
                    .map(this::toCommentDomain)
                    .forEach(post::addComment);
        }

        // @Relationship HAS_KEYWORDS
        if (node.getKeywords() != null) {
            post.setKeywords(node.getKeywords().stream()
                    .map(kn -> new Keyword(kn.getText()))
                    .toList());
        }

        return post;
    }

    private Post toDomain(PostNode node, UserId authorId) {
        Post post = new Post(
                node.getId(),
                authorId,
                new PostContent(node.getContent() != null ? node.getContent() : ""),
                PostPrivacy.valueOf(node.getPrivacy())
        );
        post.setLikeCount(node.getLikeCount());
        post.setShareCount(node.getShareCount());
        post.setCommentCount(node.getCommentCount());

        if (node.getSharedFrom() != null) {
            post.setSharedFromPostId(node.getSharedFrom().getId());
        }
        if (node.getAttachments() != null) {
            node.getAttachments().forEach(fn ->
                    post.attachFile(new FileEntity(
                            new FileMeta(fn.getPath(), fn.getName(),
                                    fn.getContentType(), fn.getSizeBytes()))));
        }
        if (node.getComments() != null) {
            node.getComments().stream()
                    .map(this::toCommentDomain)
                    .forEach(post::addComment);
        }
        if (node.getKeywords() != null) {
            post.setKeywords(node.getKeywords().stream()
                    .map(kn -> new Keyword(kn.getText())).toList());
        }
        return post;
    }

    private Comment toCommentDomain(CommentNode cn) {
        Comment comment = new Comment(
                cn.getId(),
                null,
                new CommentContent(cn.getContent() != null ? cn.getContent() : "")
        );

        // @Relationship ATTACH_FILE
        if (cn.getAttachedFile() != null) {
            FileNode fn = cn.getAttachedFile();
            comment.attachFile(new FileEntity(
                    new FileMeta(fn.getPath(), fn.getName(),
                            fn.getContentType(), fn.getSizeBytes())));
        }

        // @Relationship REPLIED
        if (cn.getReplies() != null) {
            cn.getReplies().stream()
                    .map(this::toCommentDomain)
                    .forEach(comment::addReply);
        }

        return comment;
    }

    // ── Repository impl ──────────────────────────────────────────────────

    @Override
    public Optional<Post> findById(String id) {
        return postRepo.findById(id).map(this::toDomain);
    }

    /**
     * findByAuthorId: @Query Cypher trực tiếp, không load toàn bộ UserNode.
     * Biết authorId từ query param → toDomain với authorId.
     */
    @Override
    public List<Post> findByAuthorId(UserId authorId, int limit, int offset) {
        return postRepo.findByAuthorId(authorId.getValue(), limit, offset)
                .stream()
                .map(p -> toDomain(p, authorId))
                .toList();
    }

    /**
     * findFeedForUser: @Query Cypher với điều kiện privacy rule.
     * Tránh eager-load UserNode.friends + friends.posts (O(friends × posts)).
     */
    @Override
    public List<Post> findFeedForUser(UserId userId, int limit, int offset) {
        return postRepo.findFeedForUser(userId.getValue(), limit, offset)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * searchByKeyword: @Query Cypher traverse Post→Keyword, không findAll().
     */
    @Override
    public List<Post> searchByKeyword(String keyword, int limit, int offset) {
        return postRepo.searchByKeyword(keyword, limit, offset)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean hasLiked(UserId userId, String postId) {
        return postRepo.hasLiked(userId.getValue(), postId);
    }

    /**
     * addLike: Cypher MERGE trực tiếp, không load UserNode.likedPosts.
     */
    @Override
    public void addLike(UserId userId, String postId) {
        postRepo.addLike(userId.getValue(), postId);
    }

    /**
     * removeLike: Cypher DELETE trực tiếp.
     */
    @Override
    public void removeLike(UserId userId, String postId) {
        postRepo.removeLike(userId.getValue(), postId);
    }

    /**
     * save: lưu PostNode + tạo POSTED relationship nếu có authorId.
     * @Relationship POSTED: User --POSTED--> Post, quản lý qua linkAuthor sau khi save node.
     */
    @Override
    public void save(Post post) {
        postRepo.save(toNode(post));
        // Đảm bảo POSTED relationship tồn tại nếu authorId được cung cấp
        if (post.getAuthorId() != null) {
            postRepo.linkAuthor(post.getAuthorId().getValue(), post.getId());
        }
    }

    @Override
    public void delete(String id) {
        postRepo.deleteById(id);
    }
}