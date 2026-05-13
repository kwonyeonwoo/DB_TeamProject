package com.academicshare.backend.post.repository;

import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.user.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("""
            select p
            from Post p
            where lower(p.title) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(p.content, '')) like lower(concat('%', :keyword, '%'))
            """)
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            select p
            from Post p, User u
            where p.userId = u.id
              and p.isAnonymous = false
              and u.status = :status
              and lower(u.name) like lower(concat('%', :author, '%'))
            """)
    Page<Post> searchByAuthor(
            @Param("author") String author,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    @Query("""
            select p
            from Post p
            where (:mainCategory is null or p.mainCategory = :mainCategory)
              and (:subCategory is null or p.subCategory = :subCategory)
            """)
    Page<Post> searchByCategory(
            @Param("mainCategory") String mainCategory,
            @Param("subCategory") String subCategory,
            Pageable pageable
    );
}
