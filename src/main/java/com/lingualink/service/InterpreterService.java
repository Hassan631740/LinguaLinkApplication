package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.InterpreterRequest;
import com.lingualink.dto.response.InterpreterResponse;
import com.lingualink.entity.Interpreter;
import com.lingualink.entity.User;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.mapper.InterpreterMapper;
import com.lingualink.repository.InterpreterRepository;
import com.lingualink.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterpreterService {
    private final InterpreterRepository interpreterRepository;
    private final InterpreterMapper interpreterMapper;
    private final UserRepository userRepository;

    public InterpreterService(InterpreterRepository interpreterRepository, InterpreterMapper interpreterMapper,
                             UserRepository userRepository) {
        this.interpreterRepository = interpreterRepository;
        this.interpreterMapper = interpreterMapper;
        this.userRepository = userRepository;
    }

    // Create
    public Interpreter createInterpreter(Interpreter interpreter) {
        return interpreterRepository.save(interpreter);
    }

    public InterpreterResponse createInterpreter(InterpreterRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        Interpreter interpreter = interpreterMapper.toEntity(request, user);
        Interpreter savedInterpreter = interpreterRepository.save(interpreter);
        return interpreterMapper.toResponse(savedInterpreter);
    }

    // Read
    @Transactional(readOnly = true)
    public List<Interpreter> getAllInterpreters() {
        return interpreterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagedResponse<Interpreter> getAllInterpreters(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("id");
        Page<Interpreter> page = interpreterRepository.findAll(pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Interpreter> getAllInterpretersWithFilters(PageParams pageParams, String language, 
                                                                     BigDecimal minRate, BigDecimal maxRate,
                                                                     Integer minExperience, Integer maxExperience) {
        Pageable pageable = pageParams.toPageable("ratePerHour");
        Page<Interpreter> page = interpreterRepository.findByFilters(language, minRate, maxRate, 
                                                                      minExperience, maxExperience, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<InterpreterResponse> getAllInterpretersWithFiltersAsResponse(PageParams pageParams, String language, 
                                                                                    BigDecimal minRate, BigDecimal maxRate,
                                                                                    Integer minExperience, Integer maxExperience) {
        Pageable pageable = pageParams.toPageable("ratePerHour");
        Page<Interpreter> page = interpreterRepository.findByFilters(language, minRate, maxRate, 
                                                                      minExperience, maxExperience, pageable);
        List<InterpreterResponse> content = page.getContent().stream()
                .map(interpreterMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<InterpreterResponse> getAllInterpretersAsResponse() {
        List<Interpreter> interpreters = getAllInterpreters();
        return interpreters.stream()
                .map(interpreterMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Interpreter getInterpreterById(Long id) {
        return interpreterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", id));
    }

    @Transactional(readOnly = true)
    public InterpreterResponse getInterpreterByIdAsResponse(Long id) {
        Interpreter interpreter = getInterpreterById(id);
        return interpreterMapper.toResponse(interpreter);
    }

    @Transactional(readOnly = true)
    public Optional<Interpreter> getInterpreterByUserId(Long userId) {
        return interpreterRepository.findByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public Optional<InterpreterResponse> getInterpreterByUserIdAsResponse(Long userId) {
        return interpreterRepository.findByUser_Id(userId)
                .map(interpreterMapper::toResponse);
    }

    // Update - Full update
    public Interpreter updateInterpreter(Long id, Interpreter interpreterDetails) {
        Interpreter interpreter = getInterpreterById(id);
        interpreter.setUser(interpreterDetails.getUser());
        interpreter.setLanguages(interpreterDetails.getLanguages());
        interpreter.setRatePerHour(interpreterDetails.getRatePerHour());
        interpreter.setExperienceYears(interpreterDetails.getExperienceYears());
        interpreter.setBio(interpreterDetails.getBio());
        return interpreterRepository.save(interpreter);
    }

    public InterpreterResponse updateInterpreter(Long id, InterpreterRequest request) {
        Interpreter interpreter = getInterpreterById(id);
        User user = request.getUserId() != null
                ? userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()))
                : interpreter.getUser();
        interpreterMapper.updateEntityFromRequest(request, interpreter, user);
        Interpreter savedInterpreter = interpreterRepository.save(interpreter);
        return interpreterMapper.toResponse(savedInterpreter);
    }

    // Update - Partial update
    public Interpreter patchInterpreter(Long id, Interpreter interpreterDetails) {
        Interpreter interpreter = getInterpreterById(id);
        
        if (interpreterDetails.getUser() != null) {
            interpreter.setUser(interpreterDetails.getUser());
        }
        if (interpreterDetails.getLanguages() != null) {
            interpreter.setLanguages(interpreterDetails.getLanguages());
        }
        if (interpreterDetails.getRatePerHour() != null) {
            interpreter.setRatePerHour(interpreterDetails.getRatePerHour());
        }
        if (interpreterDetails.getExperienceYears() != null) {
            interpreter.setExperienceYears(interpreterDetails.getExperienceYears());
        }
        if (interpreterDetails.getBio() != null) {
            interpreter.setBio(interpreterDetails.getBio());
        }
        return interpreterRepository.save(interpreter);
    }

    public InterpreterResponse patchInterpreter(Long id, InterpreterRequest request) {
        Interpreter interpreter = getInterpreterById(id);
        User user = request.getUserId() != null
                ? userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()))
                : null;
        interpreterMapper.updateEntityFromRequest(request, interpreter, user);
        Interpreter savedInterpreter = interpreterRepository.save(interpreter);
        return interpreterMapper.toResponse(savedInterpreter);
    }

    // Delete
    public void deleteInterpreter(Long id) {
        Interpreter interpreter = getInterpreterById(id);
        interpreterRepository.delete(interpreter);
    }
}


