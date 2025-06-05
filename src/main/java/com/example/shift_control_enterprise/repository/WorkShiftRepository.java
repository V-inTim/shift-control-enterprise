package com.example.shift_control_enterprise.repository;

import com.example.shift_control_enterprise.dto.WorkTimePerWeekInSeconds;
import com.example.shift_control_enterprise.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
    Optional<WorkShift> findByEmployeeIdAndEventDate (Long employeeId, LocalDate eventDate);

    @Query("SELECT ws FROM WorkShift ws " +
            "WHERE ws.employee.id IN :employeeIds " +
            "AND ws.eventDate BETWEEN :startDate AND :endDate " +
            "ORDER BY ws.employee.id, ws.eventDate")
    List<WorkShift> findAllByEmployeesAndPeriod(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = """
    SELECT
        EXTRACT(YEAR FROM ws.event_date) AS year,
        EXTRACT(WEEK FROM ws.event_date) AS week,
        SUM(EXTRACT(EPOCH FROM (ws.end_time - ws.start_time))) AS totalSeconds
    FROM work_shifts ws
    JOIN employees e ON ws.employee_id = e.id
    WHERE e.enterprise_id = :enterpriseId
      AND ws.event_date >= :startDate
    GROUP BY year, week
    ORDER BY year, week
    """, nativeQuery = true)
    List<WorkTimePerWeekInSeconds> getWorkTimePerWeeks(
            @Param("enterpriseId") Long enterpriseId,
            @Param("startDate") LocalDate startDate
    );

    boolean existsByIdAndEmployeeId(Long id, Long employeeId);
    boolean existsByEmployeeIdAndEventDate(Long employeeId, LocalDate eventDate);
}
