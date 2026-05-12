package com.academicshare.backend.comment.repository;

import com.academicshare.backend.comment.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByPostIdOrderByCreatedAtAscIdAsc(Integer postId);
}
