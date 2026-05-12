package com.academicshare.backend.post.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class PostFileStorageTest {

    @TempDir
    private Path tempDir;

    @Test
    void storeThrowsValidationErrorWhenUploadPathCannotBeCreated() throws Exception {
        Path blockedRoot = tempDir.resolve("blocked-root");
        Files.writeString(blockedRoot, "not a directory");
        PostFileStorage storage = new PostFileStorage(blockedRoot.toString());
        MockMultipartFile file = new MockMultipartFile("files", "content".getBytes());

        assertThatThrownBy(() -> storage.store(1, file))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_ERROR));
    }
}
