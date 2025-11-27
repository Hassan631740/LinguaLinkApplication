package com.lingualink.repository;

import com.lingualink.entity.Interpreter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterpreterRepository extends JpaRepository<Interpreter, Long> {
    Optional<Interpreter> findByUser_Id(Long userId);
}

