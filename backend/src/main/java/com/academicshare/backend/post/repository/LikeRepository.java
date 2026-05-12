package com.academicshare.backend.post.repository;

import com.academicshare.backend.post.domain.Like;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Integer> {

    boolean existsByUserIdAndPostId(Integer userId, Integer postId);

    Optional<Like> findByUserIdAndPostId(Integer userId, Integer postId);

    long countByPostId(Integer postId);
}
