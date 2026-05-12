package com.academicshare.backend.group.repository;

import com.academicshare.backend.group.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Integer> {
}
