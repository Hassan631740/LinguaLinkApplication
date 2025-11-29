package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.CallRequest;
import com.lingualink.dto.response.CallResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Call;
import com.lingualink.entity.Interpreter;
import com.lingualink.entity.User;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.mapper.CallMapper;
import com.lingualink.repository.BookingRepository;
import com.lingualink.repository.CallRepository;
import com.lingualink.repository.InterpreterRepository;
import com.lingualink.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CallService {
    private final CallRepository callRepository;
    private final CallMapper callMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final InterpreterRepository interpreterRepository;

    public CallService(CallRepository callRepository, CallMapper callMapper,
                       BookingRepository bookingRepository, UserRepository userRepository,
                       InterpreterRepository interpreterRepository) {
        this.callRepository = callRepository;
        this.callMapper = callMapper;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.interpreterRepository = interpreterRepository;
    }

    // Create
    public Call createCall(Call call) {
        return callRepository.save(call);
    }

    public CallResponse createCall(CallRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));
        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getClientId()));
        Interpreter interpreter = interpreterRepository.findById(request.getInterpreterId())
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", request.getInterpreterId()));
        Call call = callMapper.toEntity(request, booking, client, interpreter);
        Call savedCall = callRepository.save(call);
        return callMapper.toResponse(savedCall);
    }

    // Read
    @Transactional(readOnly = true)
    public List<Call> getAllCalls() {
        return callRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagedResponse<Call> getAllCalls(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("startTime");
        Page<Call> page = callRepository.findAll(pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Call> getAllCallsWithFilters(PageParams pageParams, Long bookingId, Long clientId,
                                                       Long interpreterId, String status,
                                                       LocalDateTime startTimeFrom, LocalDateTime startTimeTo) {
        Pageable pageable = pageParams.toPageable("startTime");
        Page<Call> page = callRepository.findByFilters(bookingId, clientId, interpreterId, status,
                                                        startTimeFrom, startTimeTo, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CallResponse> getAllCallsWithFiltersAsResponse(PageParams pageParams, Long bookingId, Long clientId,
                                                                         Long interpreterId, String status,
                                                                         LocalDateTime startTimeFrom, LocalDateTime startTimeTo) {
        Pageable pageable = pageParams.toPageable("startTime");
        Page<Call> page = callRepository.findByFilters(bookingId, clientId, interpreterId, status,
                                                        startTimeFrom, startTimeTo, pageable);
        List<CallResponse> content = page.getContent().stream()
                .map(callMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<CallResponse> getAllCallsAsResponse() {
        List<Call> calls = getAllCalls();
        return calls.stream()
                .map(callMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Call getCallById(Long id) {
        return callRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Call", "id", id));
    }

    @Transactional(readOnly = true)
    public CallResponse getCallByIdAsResponse(Long id) {
        Call call = getCallById(id);
        return callMapper.toResponse(call);
    }

    @Transactional(readOnly = true)
    public List<Call> getCallsByBookingId(Long bookingId) {
        return callRepository.findByBooking_Id(bookingId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CallResponse> getCallsByBookingIdAsResponse(Long bookingId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("startTime");
        Page<Call> page = callRepository.findByBooking_Id(bookingId, pageable);
        List<CallResponse> content = page.getContent().stream()
                .map(callMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<CallResponse> getCallsByBookingIdAsResponse(Long bookingId) {
        List<Call> calls = getCallsByBookingId(bookingId);
        return calls.stream()
                .map(callMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Call> getCallsByClientId(Long clientId) {
        return callRepository.findByClient_Id(clientId);
    }

    @Transactional(readOnly = true)
    public List<CallResponse> getCallsByClientIdAsResponse(Long clientId) {
        List<Call> calls = getCallsByClientId(clientId);
        return calls.stream()
                .map(callMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Call> getCallsByInterpreterId(Long interpreterId) {
        return callRepository.findByInterpreter_Id(interpreterId);
    }

    @Transactional(readOnly = true)
    public List<CallResponse> getCallsByInterpreterIdAsResponse(Long interpreterId) {
        List<Call> calls = getCallsByInterpreterId(interpreterId);
        return calls.stream()
                .map(callMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Call> getCallsByStatus(String status) {
        return callRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<CallResponse> getCallsByStatusAsResponse(String status) {
        List<Call> calls = getCallsByStatus(status);
        return calls.stream()
                .map(callMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Update - Full update
    public Call updateCall(Long id, Call callDetails) {
        Call call = getCallById(id);
        call.setBooking(callDetails.getBooking());
        call.setClient(callDetails.getClient());
        call.setInterpreter(callDetails.getInterpreter());
        call.setCallDurationSeconds(callDetails.getCallDurationSeconds());
        call.setStartTime(callDetails.getStartTime());
        call.setEndTime(callDetails.getEndTime());
        call.setStatus(callDetails.getStatus());
        call.setRecordingUrl(callDetails.getRecordingUrl());
        call.setNotes(callDetails.getNotes());
        call.setQualityRating(callDetails.getQualityRating());
        return callRepository.save(call);
    }

    public CallResponse updateCall(Long id, CallRequest request) {
        Call call = getCallById(id);
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : call.getBooking();
        User client = request.getClientId() != null
                ? userRepository.findById(request.getClientId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getClientId()))
                : call.getClient();
        Interpreter interpreter = request.getInterpreterId() != null
                ? interpreterRepository.findById(request.getInterpreterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", request.getInterpreterId()))
                : call.getInterpreter();
        callMapper.updateEntityFromRequest(request, call, booking, client, interpreter);
        Call savedCall = callRepository.save(call);
        return callMapper.toResponse(savedCall);
    }

    // Update - Partial update
    public Call patchCall(Long id, Call callDetails) {
        Call call = getCallById(id);
        
        if (callDetails.getBooking() != null) {
            call.setBooking(callDetails.getBooking());
        }
        if (callDetails.getClient() != null) {
            call.setClient(callDetails.getClient());
        }
        if (callDetails.getInterpreter() != null) {
            call.setInterpreter(callDetails.getInterpreter());
        }
        if (callDetails.getCallDurationSeconds() != null) {
            call.setCallDurationSeconds(callDetails.getCallDurationSeconds());
        }
        if (callDetails.getStartTime() != null) {
            call.setStartTime(callDetails.getStartTime());
        }
        if (callDetails.getEndTime() != null) {
            call.setEndTime(callDetails.getEndTime());
        }
        if (callDetails.getStatus() != null) {
            call.setStatus(callDetails.getStatus());
        }
        if (callDetails.getRecordingUrl() != null) {
            call.setRecordingUrl(callDetails.getRecordingUrl());
        }
        if (callDetails.getNotes() != null) {
            call.setNotes(callDetails.getNotes());
        }
        if (callDetails.getQualityRating() != null) {
            call.setQualityRating(callDetails.getQualityRating());
        }
        return callRepository.save(call);
    }

    public CallResponse patchCall(Long id, CallRequest request) {
        Call call = getCallById(id);
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : null;
        User client = request.getClientId() != null
                ? userRepository.findById(request.getClientId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getClientId()))
                : null;
        Interpreter interpreter = request.getInterpreterId() != null
                ? interpreterRepository.findById(request.getInterpreterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", request.getInterpreterId()))
                : null;
        callMapper.updateEntityFromRequest(request, call, booking, client, interpreter);
        Call savedCall = callRepository.save(call);
        return callMapper.toResponse(savedCall);
    }

    // Delete
    public void deleteCall(Long id) {
        Call call = getCallById(id);
        callRepository.delete(call);
    }
}

