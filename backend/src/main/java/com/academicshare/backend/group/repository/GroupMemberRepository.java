package com.academicshare.backend.group.repository;

import com.academicshare.backend.group.domain.GroupMember;
import com.academicshare.backend.group.domain.GroupMemberId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {

    boolean existsByGroupIdAndUserId(Integer groupId, Integer userId);

    List<GroupMember> findByGroupIdOrderByJoinedAtAscUserIdAsc(Integer groupId);

    List<GroupMember> findByUserIdOrderByJoinedAtAscGroupIdAsc(Integer userId);
}
