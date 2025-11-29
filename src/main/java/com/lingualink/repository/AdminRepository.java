package com.lingualink.repository;

import com.lingualink.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUser_Id(Long userId);
    Optional<Admin> findByEmployeeId(String employeeId);
    List<Admin> findByDepartment(String department);
    List<Admin> findByAccessLevel(String accessLevel);
    List<Admin> findByIsActive(Boolean isActive);
    
    // Pagination support
    Page<Admin> findAll(Pageable pageable);
    Page<Admin> findByDepartment(String department, Pageable pageable);
    Page<Admin> findByAccessLevel(String accessLevel, Pageable pageable);
    Page<Admin> findByIsActive(Boolean isActive, Pageable pageable);
    
    // Advanced filtering
    @Query("SELECT a FROM Admin a WHERE " +
           "(:department IS NULL OR a.department = :department) AND " +
           "(:accessLevel IS NULL OR a.accessLevel = :accessLevel) AND " +
           "(:isActive IS NULL OR a.isActive = :isActive)")
    Page<Admin> findByFilters(@Param("department") String department,
                               @Param("accessLevel") String accessLevel,
                               @Param("isActive") Boolean isActive,
                               Pageable pageable);
    
    boolean existsByEmployeeId(String employeeId);
    boolean existsByUser_Id(Long userId);
}

