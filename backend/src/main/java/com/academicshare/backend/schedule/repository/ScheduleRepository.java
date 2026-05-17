package com.academicshare.backend.schedule.repository;

import com.academicshare.backend.schedule.domain.Schedule;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    @Query("""
            select s
            from Schedule s
            where s.userId = :userId
              and s.groupId is null
              and (:startAt is null or s.endAt >= :startAt)
              and (:endAt is null or s.startAt <= :endAt)
            order by s.startAt asc, s.id asc
            """)
    List<Schedule> findPersonalSchedules(
            @Param("userId") Integer userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("""
            select s
            from Schedule s
            where s.groupId = :groupId
              and (:startAt is null or s.endAt >= :startAt)
              and (:endAt is null or s.startAt <= :endAt)
            order by s.startAt asc, s.id asc
            """)
    List<Schedule> findGroupSchedules(
            @Param("groupId") Integer groupId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    void deleteByUserIdAndGroupIdIsNull(Integer userId);
}
