package com.academicshare.backend.post.repository;

import com.academicshare.backend.post.domain.PostFile;
import com.academicshare.backend.post.domain.PostFileId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostFileRepository extends JpaRepository<PostFile, PostFileId> {

    List<PostFile> findByIdInOrderByIdAscFileUrlAsc(Collection<Integer> ids);

    List<PostFile> findByIdOrderByFileUrlAsc(Integer id);

    @Modifying
    @Query("delete from PostFile file where file.id = :postId")
    void deleteAllByPostId(@Param("postId") Integer postId);
}
