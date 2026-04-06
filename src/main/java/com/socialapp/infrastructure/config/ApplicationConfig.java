package com.socialapp.infrastructure.config;

import com.socialapp.application.account.usecase.Register.ConfirmEmailUseCase;
import com.socialapp.application.account.usecase.Register.RegisterUseCase;
import com.socialapp.application.account.usecase.login.*;
import com.socialapp.application.account.usecase.logout.LogoutUseCase;

import com.socialapp.application.comment.usecase.*;
import com.socialapp.application.message.usecase.*;
import com.socialapp.application.notification.usecase.GetNotificationsUseCase;
import com.socialapp.application.post.usecase.*;
import com.socialapp.application.post.usecase.postInteraction.*;
import com.socialapp.application.post.usecase.postMutation.*;
import com.socialapp.application.relationship.usecase.*;
import com.socialapp.application.user.usecase.*;

import com.socialapp.application.shared.port.EmailSender;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.application.shared.port.TokenProvider;

import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.service.AccountDomainService;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.domain.comment.service.CommentDomainService;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.domain.message.repository.MessageRepository;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.service.NotificationDomainService;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.post.service.PostDomainService;
import com.socialapp.domain.relationship.repository.BlockRepository;
import com.socialapp.domain.relationship.repository.FriendRepository;
import com.socialapp.domain.relationship.repository.FriendRequestRepository;
import com.socialapp.domain.relationship.service.RelationshipDomainService;
import com.socialapp.domain.user.repository.UserRepository;
import com.socialapp.domain.user.service.UserDomainService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

    // ==================== DOMAIN SERVICES ====================

    @Bean
    public UserDomainService userDomainService() {
        return new UserDomainService();
    }

    @Bean
    public NotificationDomainService notificationDomainService() {
        return new NotificationDomainService();
    }

    @Bean
    public PostDomainService postDomainService() {
        return new PostDomainService();
    }

    @Bean
    public RelationshipDomainService relationshipDomainService() {
        return new RelationshipDomainService();
    }

    @Bean
    public AccountDomainService accountDomainService(
            AccountDomainService.PasswordMatcher passwordMatcher) {
        return new AccountDomainService(passwordMatcher);
    }

    @Bean
    public CommentDomainService commentDomainService() {
        return new CommentDomainService();
    }

    // ==================== ACCOUNT USE CASES ====================

    @Bean
    public RegisterUseCase registerUseCase(
            AccountRepository accountRepository,
            UserRepository userRepository,
            AccountDomainService accountDomainService,
            EmailSender emailSender,
            PasswordEncoder passwordEncoder) {
        return new RegisterUseCase(accountRepository, userRepository,
                accountDomainService, emailSender, passwordEncoder);
    }

    @Bean
    public ConfirmEmailUseCase confirmEmailUseCase(
            AccountRepository accountRepository,
            TokenProvider tokenProvider) {
        return new ConfirmEmailUseCase(accountRepository, tokenProvider);
    }

    @Bean
    public LoginUseCase loginUseCase(
            AccountRepository accountRepository,
            AccountDomainService accountDomainService,
            TokenProvider tokenProvider,
            PasswordEncoder passwordEncoder) {
        return new LoginUseCase(accountRepository, accountDomainService,
                tokenProvider, passwordEncoder);
    }

    @Bean
    public PrepareResetPasswordUseCase prepareResetPasswordUseCase(
            AccountRepository accountRepository,
            AccountDomainService accountDomainService,
            EmailSender emailSender) {
        return new PrepareResetPasswordUseCase(accountRepository,
                accountDomainService, emailSender);
    }

    @Bean
    public ConfirmResetCodeUseCase confirmResetCodeUseCase(
            AccountRepository accountRepository) {
        return new ConfirmResetCodeUseCase(accountRepository);
    }

    @Bean
    public UpdatePasswordUseCase updatePasswordUseCase(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        return new UpdatePasswordUseCase(accountRepository, passwordEncoder);
    }

    @Bean
    public LogoutUseCase logoutUseCase(
            LogoutUseCase.TokenBlacklist tokenBlacklist) {
        return new LogoutUseCase(tokenBlacklist);
    }

    // ==================== USER USE CASES ====================

    @Bean
    public ChangeBioUseCase changeBioUseCase(UserRepository userRepository) {
        return new ChangeBioUseCase(userRepository);
    }

    @Bean
    public ChangeBirthdateUseCase changeBirthdateUseCase(UserRepository userRepository) {
        return new ChangeBirthdateUseCase(userRepository);
    }

    @Bean
    public ChangeNameUseCase changeNameUseCase(
            UserRepository userRepository,
            UserDomainService userDomainService) {
        return new ChangeNameUseCase(userRepository, userDomainService);
    }

    @Bean
    public ChangeUsernameUseCase changeUsernameUseCase(
            UserRepository userRepository,
            UserDomainService userDomainService) {
        return new ChangeUsernameUseCase(userRepository, userDomainService);
    }

    @Bean
    // ✅ FIX: inject thêm FileStorage để convert path → URL
    public GetProfileUseCase getProfileUseCase(
            UserRepository userRepository,
            BlockRepository blockRepository,
            FriendRepository friendRepository,
            FriendRequestRepository friendRequestRepository,
            UserDomainService userDomainService,
            FileStorage fileStorage) {
        return new GetProfileUseCase(userRepository, blockRepository,
                friendRepository, friendRequestRepository, userDomainService, fileStorage);
    }

    @Bean
    // ✅ FIX: inject thêm FileStorage
    public SearchUserUseCase searchUserUseCase(
            UserRepository userRepository,
            FileStorage fileStorage) {
        return new SearchUserUseCase(userRepository, fileStorage);
    }

    @Bean
    public UpdateProfilePictureUseCase updateProfilePictureUseCase(
            UserRepository userRepository,
            FileStorage fileStorage,
            FileRepository fileRepository) {
        return new UpdateProfilePictureUseCase(userRepository, fileStorage, fileRepository);
    }

    // ==================== RELATIONSHIP USE CASES ====================

    @Bean
    public SendFriendRequestUseCase sendFriendRequestUseCase(
            UserRepository userRepository,
            FriendRepository friendRepository,
            FriendRequestRepository friendRequestRepository,
            BlockRepository blockRepository,
            RelationshipDomainService relationshipDomainService,
            NotificationRepository notificationRepository,
            NotificationDomainService notificationDomainService,
            RealtimePublisher realtimePublisher) {
        return new SendFriendRequestUseCase(userRepository, friendRepository,
                friendRequestRepository, blockRepository, relationshipDomainService,
                notificationRepository, notificationDomainService, realtimePublisher);
    }

    @Bean
    public AcceptFriendRequestUseCase acceptFriendRequestUseCase(
            UserRepository userRepository,
            FriendRepository friendRepository,
            FriendRequestRepository friendRequestRepository,
            RelationshipDomainService relationshipDomainService,
            NotificationRepository notificationRepository,
            NotificationDomainService notificationDomainService,
            RealtimePublisher realtimePublisher) {
        return new AcceptFriendRequestUseCase(userRepository, friendRepository,
                friendRequestRepository, relationshipDomainService,
                notificationRepository, notificationDomainService, realtimePublisher);
    }

    @Bean
    public DeleteFriendRequestUseCase deleteFriendRequestUseCase(
            UserRepository userRepository,
            FriendRequestRepository friendRequestRepository,
            RelationshipDomainService relationshipDomainService) {
        return new DeleteFriendRequestUseCase(userRepository,
                friendRequestRepository, relationshipDomainService);
    }

    @Bean
    public UnfriendUseCase unfriendUseCase(
            UserRepository userRepository,
            FriendRepository friendRepository,
            RelationshipDomainService relationshipDomainService) {
        return new UnfriendUseCase(userRepository, friendRepository, relationshipDomainService);
    }

    @Bean
    public BlockUserUseCase blockUserUseCase(
            UserRepository userRepository,
            BlockRepository blockRepository,
            FriendRepository friendRepository,
            FriendRequestRepository friendRequestRepository,
            RelationshipDomainService relationshipDomainService) {
        return new BlockUserUseCase(userRepository, blockRepository,
                friendRepository, friendRequestRepository, relationshipDomainService);
    }

    @Bean
    public UnblockUserUseCase unblockUserUseCase(
            UserRepository userRepository,
            BlockRepository blockRepository,
            RelationshipDomainService relationshipDomainService) {
        return new UnblockUserUseCase(userRepository, blockRepository, relationshipDomainService);
    }

    // ==================== POST USE CASES ====================

    @Bean
    // ✅ FIX: inject FileStorage để convert paths → URLs trong response
    public CreatePostUseCase createPostUseCase(
            PostRepository postRepository,
            FileStorage fileStorage,
            FileRepository fileRepository) {
        return new CreatePostUseCase(postRepository, fileStorage, fileRepository);
    }

    @Bean
    // ✅ FIX: inject FileStorage
    public GetPostUseCase getPostUseCase(
            PostRepository postRepository,
            PostDomainService postDomainService,
            FriendRepository friendRepository,
            FileStorage fileStorage) {
        return new GetPostUseCase(postRepository, postDomainService, friendRepository, fileStorage);
    }

    @Bean
    // ✅ FIX: inject FileStorage — GetFeedUseCase không còn @Service nên phải khai báo bean
    public GetFeedUseCase getFeedUseCase(
            PostRepository postRepository,
            FileStorage fileStorage) {
        return new GetFeedUseCase(postRepository, fileStorage);
    }

    @Bean
    // ✅ FIX: inject FileStorage
    public GetPostsByAuthorUseCase getPostsByAuthorUseCase(
            PostRepository postRepository,
            FileStorage fileStorage) {
        return new GetPostsByAuthorUseCase(postRepository, fileStorage);
    }

    @Bean
    // ✅ FIX: inject FileStorage
    public SearchPostsUseCase searchPostsUseCase(
            PostRepository postRepository,
            FileStorage fileStorage) {
        return new SearchPostsUseCase(postRepository, fileStorage);
    }

    @Bean
    public UpdatePostContentUseCase updatePostContentUseCase(
            PostRepository postRepository,
            FileStorage fileStorage,
            FileRepository fileRepository) {
        return new UpdatePostContentUseCase(postRepository, fileStorage, fileRepository);
    }

    @Bean
    public UpdatePostPrivacyUseCase updatePostPrivacyUseCase(PostRepository postRepository) {
        return new UpdatePostPrivacyUseCase(postRepository);
    }

    @Bean
    public DeletePostUseCase deletePostUseCase(
            PostRepository postRepository,
            FileStorage fileStorage,
            FileRepository fileRepository) {
        return new DeletePostUseCase(postRepository, fileStorage, fileRepository);
    }

    @Bean
    public LikePostUseCase likePostUseCase(
            PostRepository postRepository,
            PostDomainService postDomainService,
            NotificationRepository notificationRepository,
            NotificationDomainService notificationDomainService,
            RealtimePublisher realtimePublisher) {
        return new LikePostUseCase(postRepository, postDomainService,
                notificationRepository, notificationDomainService, realtimePublisher);
    }

    @Bean
    public UnlikePostUseCase unlikePostUseCase(
            PostRepository postRepository,
            PostDomainService postDomainService) {
        return new UnlikePostUseCase(postRepository, postDomainService);
    }

    @Bean
    public SharePostUseCase sharePostUseCase(
            PostRepository postRepository,
            PostDomainService postDomainService) {
        return new SharePostUseCase(postRepository, postDomainService);
    }

    // ==================== COMMENT USE CASES ====================

    @Bean
    // FIX: inject UserRepository để trả authorUsername + authorProfilePic thật
    public GetCommentsUseCase getCommentsUseCase(
            CommentRepository commentRepository,
            PostRepository postRepository,
            FileStorage fileStorage,
            UserRepository userRepository) {
        return new GetCommentsUseCase(commentRepository, postRepository, fileStorage, userRepository);
    }

    @Bean
    // ✅ FIX Bug 3: inject UserRepository để trả authorUsername thật về frontend
    public CreateCommentUseCase createCommentUseCase(
            CommentRepository commentRepository,
            PostRepository postRepository,
            FileStorage fileStorage,
            FileRepository fileRepository,
            NotificationRepository notificationRepository,
            NotificationDomainService notificationDomainService,
            RealtimePublisher realtimePublisher,
            UserRepository userRepository) {
        return new CreateCommentUseCase(commentRepository, postRepository,
                fileStorage, fileRepository, notificationRepository,
                notificationDomainService, realtimePublisher, userRepository);
    }

    @Bean
    public DeleteCommentUseCase deleteCommentUseCase(
            CommentRepository commentRepository,
            PostRepository postRepository,
            FileStorage fileStorage,
            FileRepository fileRepository) {
        return new DeleteCommentUseCase(commentRepository, postRepository,
                fileStorage, fileRepository);
    }

    @Bean
    public UpdateCommentUseCase updateCommentUseCase(
            CommentRepository commentRepository,
            FileStorage fileStorage,
            FileRepository fileRepository) {
        return new UpdateCommentUseCase(commentRepository, fileStorage, fileRepository);
    }

    @Bean
    public LikeCommentUseCase likeCommentUseCase(
            CommentRepository commentRepository,
            CommentDomainService commentDomainService,
            NotificationRepository notificationRepository,
            NotificationDomainService notificationDomainService,
            RealtimePublisher realtimePublisher) {
        return new LikeCommentUseCase(commentRepository, commentDomainService,
                notificationRepository, notificationDomainService, realtimePublisher);
    }

    @Bean
    public UnlikeCommentUseCase unlikeCommentUseCase(
            CommentRepository commentRepository,
            CommentDomainService commentDomainService) {
        return new UnlikeCommentUseCase(commentRepository, commentDomainService);
    }

    @Bean
    // ✅ FIX Bug 3: inject UserRepository để trả authorUsername thật về frontend
    public ReplyCommentUseCase replyCommentUseCase(
            CommentRepository commentRepository,
            PostRepository postRepository,
            CommentDomainService commentDomainService,
            FileStorage fileStorage,
            FileRepository fileRepository,
            NotificationRepository notificationRepository,
            NotificationDomainService notificationDomainService,
            RealtimePublisher realtimePublisher,
            UserRepository userRepository) {
        return new ReplyCommentUseCase(commentRepository, postRepository,
                commentDomainService, fileStorage, fileRepository,
                notificationRepository, notificationDomainService, realtimePublisher,
                userRepository);
    }

    // ==================== MESSAGE USE CASES ====================

    @Bean
    public SendMessageUseCase sendMessageUseCase(
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            FileStorage fileStorage,
            FileRepository fileRepository,
            RealtimePublisher realtimePublisher) {
        return new SendMessageUseCase(chatRepository, messageRepository,
                fileStorage, fileRepository, realtimePublisher);
    }

    @Bean
    public UpdateMessageUseCase updateMessageUseCase(
            MessageRepository messageRepository,
            ChatRepository chatRepository,
            RealtimePublisher realtimePublisher) {
        return new UpdateMessageUseCase(messageRepository, chatRepository, realtimePublisher);
    }

    @Bean
    public DeleteMessageUseCase deleteMessageUseCase(
            MessageRepository messageRepository,
            FileStorage fileStorage,
            FileRepository fileRepository,
            RealtimePublisher realtimePublisher) {
        return new DeleteMessageUseCase(messageRepository, fileStorage,
                fileRepository, realtimePublisher);
    }

    @Bean
    public GetChatUseCase getChatUseCase(
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            FileStorage fileStorage) {
        return new GetChatUseCase(chatRepository, messageRepository, fileStorage);
    }

    @Bean
    public GetChatListUseCase getChatListUseCase(ChatRepository chatRepository) {
        return new GetChatListUseCase(chatRepository);
    }

    @Bean
    public SearchChatUseCase searchChatUseCase(ChatRepository chatRepository) {
        return new SearchChatUseCase(chatRepository);
    }

    // ==================== NOTIFICATION USE CASES ====================

    @Bean
    public GetNotificationsUseCase getNotificationsUseCase(
            NotificationRepository notificationRepository) {
        return new GetNotificationsUseCase(notificationRepository);
    }
}