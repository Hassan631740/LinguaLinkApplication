package com.lingualink.repository;

import com.lingualink.entity.Interpreter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface InterpreterRepository extends JpaRepository<Interpreter, Long> {
    Optional<Interpreter> findByUser_Id(Long userId);
    
    // Pagination support
    Page<Interpreter> findAll(Pageable pageable);
    
    // Advanced filtering
    @Query("SELECT i FROM Interpreter i WHERE " +
           "(:language IS NULL OR i.languages LIKE CONCAT('%', :language, '%')) AND " +
           "(:minRate IS NULL OR i.ratePerHour >= :minRate) AND " +
           "(:maxRate IS NULL OR i.ratePerHour <= :maxRate) AND " +
           "(:minExperience IS NULL OR i.experienceYears >= :minExperience) AND " +
           "(:maxExperience IS NULL OR i.experienceYears <= :maxExperience)")
    Page<Interpreter> findByFilters(@Param("language") String language,
                                    @Param("minRate") BigDecimal minRate,
                                    @Param("maxRate") BigDecimal maxRate,
                                    @Param("minExperience") Integer minExperience,
                                    @Param("maxExperience") Integer maxExperience,
                                    Pageable pageable);
}

