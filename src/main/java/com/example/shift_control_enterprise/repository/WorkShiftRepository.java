package com.example.shift_control_enterprise.repository;

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

    boolean existsByIdAndEmployeeId(Long id, Long employeeId);
    boolean existsByEmployeeIdAndEventDate(Long employeeId, LocalDate eventDate);
}
