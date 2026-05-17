package com.academicshare.backend.group.repository;

import com.academicshare.backend.group.domain.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Integer> {

    Optional<Group> findByGroupCode(String groupCode);

    boolean existsByGroupCode(String groupCode);

    @Query(value = """
            select g.*
            from `groups` g
            join group_members gm on gm.group_id = g.id
            where gm.user_id = :userId
            order by g.created_at asc, g.id asc
            """, nativeQuery = true)
    List<Group> findJoinedGroupsByUserId(@Param("userId") Integer userId);
}
