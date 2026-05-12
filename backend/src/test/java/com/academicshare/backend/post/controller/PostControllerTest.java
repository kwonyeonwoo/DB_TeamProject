package com.academicshare.backend.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academicshare.backend.auth.session.AuthSessionAttributes;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.post.domain.Like;
import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.domain.PostFile;
import com.academicshare.backend.post.repository.LikeRepository;
import com.academicshare.backend.post.repository.PostFileRepository;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.upload.root=build/test-uploads")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostFileRepository postFileRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Test
    void getPostListReturnsNewestPostsWithLikeFields() throws Exception {
        User currentUser = saveUser("list-current", "List Current", "list-current@example.com");
        User author = saveUser("list-author", "List Author", "list-author@example.com");
        Post olderPost = savePost(author, "Older title", "older content", "Major", "Subject", false);
        Post newerPost = savePost(author, "Newer title", "newer content", "Major", "Subject", false);
        likeRepository.saveAndFlush(new Like(currentUser.getId(), newerPost.getId()));

        mockMvc.perform(get("/posts")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id").value(newerPost.getId()))
                .andExpect(jsonPath("$.items[0].author_display_name").value("List Author"))
                .andExpect(jsonPath("$.items[0].liked_by_me").value(true))
                .andExpect(jsonPath("$.items[0].like_count").value(1))
                .andExpect(jsonPath("$.items[1].id").value(olderPost.getId()))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total_count").value(2));
    }

    @Test
    void getPostListRejectsMultipleFilterKindsAndInvalidPage() throws Exception {
        User currentUser = saveUser("list-invalid", "List Invalid", "list-invalid@example.com");

        mockMvc.perform(get("/posts")
                        .param("keyword", "title")
                        .param("author", "author")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(get("/posts")
                        .param("page", "0")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void getPostDetailIncrementsViewCountAndReturns404WhenMissing() throws Exception {
        User currentUser = saveUser("detail-current", "Detail Current", "detail-current@example.com");
        Post post = savePost(currentUser, "Detail title", "detail content", "Major", "Subject", false);

        mockMvc.perform(get("/posts/{postId}", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.view_count").value(1));

        assertThat(postRepository.findById(post.getId()).orElseThrow().getViewCount()).isEqualTo(1);

        mockMvc.perform(get("/posts/{postId}", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void createPostStoresUploadedFileAndRejectsMissingRequiredField() throws Exception {
        User currentUser = saveUser("create-current", "Create Current", "create-current@example.com");
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "notes.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "file-content".getBytes(StandardCharsets.UTF_8)
        );

        MvcResult result = mockMvc.perform(multipart("/posts")
                        .file(file)
                        .param("title", "Created title")
                        .param("content", "Created content")
                        .param("main_category", "Major")
                        .param("sub_category", "Subject")
                        .param("is_anonymous", "false")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Created title"))
                .andExpect(jsonPath("$.files.length()").value(1))
                .andExpect(jsonPath("$.files[0].file_url", startsWith("/uploads/posts/")))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        Integer postId = response.get("id").asInt();
        assertThat(postFileRepository.findByIdOrderByFileUrlAsc(postId))
                .extracting(PostFile::getFileUrl)
                .allMatch(fileUrl -> fileUrl.startsWith("/uploads/posts/" + postId + "/"));

        mockMvc.perform(multipart("/posts")
                        .param("content", "Missing title")
                        .param("main_category", "Major")
                        .param("sub_category", "Subject")
                        .param("is_anonymous", "false")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void updatePostSupportsJsonAndRejectsInvalidAuthorOrEmptyPatch() throws Exception {
        User owner = saveUser("update-owner", "Update Owner", "update-owner@example.com");
        User other = saveUser("update-other", "Update Other", "update-other@example.com");
        Post post = savePost(owner, "Before", "before content", "Major", "Subject", false);

        mockMvc.perform(patch("/posts/{postId}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "After",
                                  "is_anonymous": true
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("After"))
                .andExpect(jsonPath("$.is_anonymous").value(true))
                .andExpect(jsonPath("$.updated_at").isNotEmpty());

        mockMvc.perform(patch("/posts/{postId}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/posts/{postId}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Blocked\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, other.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));
    }

    @Test
    void updatePostByMultipartReplacesFiles() throws Exception {
        User owner = saveUser("file-owner", "File Owner", "file-owner@example.com");
        Post post = savePost(owner, "File post", "content", "Major", "Subject", false);
        postFileRepository.saveAndFlush(new PostFile(post.getId(), "/uploads/posts/" + post.getId() + "/old-file"));
        MockMultipartFile newFile = new MockMultipartFile(
                "files",
                "new.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "new-file".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/posts/{postId}", post.getId())
                        .file(newFile)
                        .param("title", "File post updated")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("File post updated"))
                .andExpect(jsonPath("$.files.length()").value(1))
                .andExpect(jsonPath("$.files[0].file_url", startsWith("/uploads/posts/" + post.getId() + "/")));

        assertThat(postFileRepository.findByIdOrderByFileUrlAsc(post.getId()))
                .extracting(PostFile::getFileUrl)
                .doesNotContain("/uploads/posts/" + post.getId() + "/old-file")
                .hasSize(1);
    }

    @Test
    void deletePostRequiresAuthorAndReturns204() throws Exception {
        User owner = saveUser("delete-owner", "Delete Owner", "delete-owner@example.com");
        User other = saveUser("delete-other", "Delete Other", "delete-other@example.com");
        Post post = savePost(owner, "Delete post", "content", "Major", "Subject", false);

        mockMvc.perform(delete("/posts/{postId}", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, other.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(delete("/posts/{postId}", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(postRepository.existsById(post.getId())).isFalse();
    }

    @Test
    void createAndDeleteLikeHandleDuplicateAndMissingLike() throws Exception {
        User currentUser = saveUser("like-current", "Like Current", "like-current@example.com");
        Post post = savePost(currentUser, "Like post", "content", "Major", "Subject", false);

        mockMvc.perform(post("/posts/{postId}/likes", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value(currentUser.getId()))
                .andExpect(jsonPath("$.post_id").value(post.getId()));

        mockMvc.perform(post("/posts/{postId}/likes", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()));

        mockMvc.perform(delete("/posts/{postId}/likes", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/posts/{postId}/likes", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private User saveUser(String loginId, String name, String emailAddress) {
        return userRepository.saveAndFlush(new User(
                loginId,
                "encoded-password",
                name,
                emailAddress
        ));
    }

    private Post savePost(User user, String title, String content, String mainCategory, String subCategory, boolean anonymous) {
        return postRepository.saveAndFlush(new Post(
                user.getId(),
                title,
                content,
                mainCategory,
                subCategory,
                anonymous
        ));
    }
}
