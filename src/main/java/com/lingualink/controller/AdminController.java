package com.lingualink.controller;

import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.AdminRequest;
import com.lingualink.dto.response.AdminResponse;
import com.lingualink.dto.response.BookingResponse;
import com.lingualink.dto.response.UserResponse;
import com.lingualink.service.AdminService;
import com.lingualink.service.BookingService;
import com.lingualink.service.UserService;
import com.lingualink.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Administrator management API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;
    private final BookingService bookingService;

    public AdminController(AdminService adminService, UserService userService, BookingService bookingService) {
        this.adminService = adminService;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    // ADMIN ENTITY CRUD OPERATIONS

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Create a new admin", description = "Creates a new admin profile in the system (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Admin already exists")
    })
    public ResponseEntity<AdminResponse> createAdmin(@Valid @RequestBody AdminRequest request) {
        AdminResponse createdAdmin = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/admin/" + createdAdmin.getId())
                .body(createdAdmin);
    }

    // READ
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Get all admins", description = "Retrieves a paginated list of all admins with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of admins"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> getAllAdmins(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir,
            @Parameter(description = "Filter by department") @RequestParam(required = false) String department,
            @Parameter(description = "Filter by access level") @RequestParam(required = false) String accessLevel,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive) {
        
        if (page != null || size != null || department != null || accessLevel != null || isActive != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<AdminResponse> pagedResponse = adminService.getAllAdminsWithFiltersAsResponse(
                    pageParams, department, accessLevel, isActive);
            return ResponseEntity.ok(pagedResponse);
        }
        
        var allAdmins = adminService.getAllAdminsAsResponse();
        return ResponseEntity.ok(allAdmins);
    }

    @GetMapping("/admins/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Get admin by ID", description = "Retrieves an admin by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin found"),
            @ApiResponse(responseCode = "404", description = "Admin not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AdminResponse> getAdminById(
            @Parameter(description = "Admin ID") @PathVariable Long id) {
        AdminResponse admin = adminService.getAdminByIdAsResponse(id);
        return ResponseEntity.ok(admin);
    }

    @GetMapping("/admins/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Get admin by user ID", description = "Retrieves an admin by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin found"),
            @ApiResponse(responseCode = "404", description = "Admin not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AdminResponse> getAdminByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        AdminResponse admin = adminService.getAdminByUserIdAsResponse(userId);
        return ResponseEntity.ok(admin);
    }

    // UPDATE
    @PutMapping("/admins/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Update admin", description = "Fully updates an existing admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin updated successfully"),
            @ApiResponse(responseCode = "404", description = "Admin not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Conflict with existing admin")
    })
    public ResponseEntity<AdminResponse> updateAdmin(
            @Parameter(description = "Admin ID") @PathVariable Long id,
            @Valid @RequestBody AdminRequest request) {
        AdminResponse updatedAdmin = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(updatedAdmin);
    }

    @PatchMapping("/admins/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Partially update admin", description = "Partially updates an existing admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin updated successfully"),
            @ApiResponse(responseCode = "404", description = "Admin not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Conflict with existing admin")
    })
    public ResponseEntity<AdminResponse> patchAdmin(
            @Parameter(description = "Admin ID") @PathVariable Long id,
            @Valid @RequestBody AdminRequest request) {
        AdminResponse updatedAdmin = adminService.patchAdmin(id, request);
        return ResponseEntity.ok(updatedAdmin);
    }

    // DELETE
    @DeleteMapping("/admins/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Delete admin", description = "Deletes an admin by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Admin deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Admin not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteAdmin(
            @Parameter(description = "Admin ID") @PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // DASHBOARD AND ADMIN OPERATIONS
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Get admin dashboard statistics", description = "Retrieves dashboard statistics for administrators")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard statistics"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = Map.of(
                "totalUsers", userService.getAllUsers().size(),
                "totalBookings", bookingService.getAllBookings().size(),
                "totalAdmins", adminService.getAllAdmins().size(),
                "message", "Dashboard statistics - to be implemented with detailed metrics"
        );
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Get all users (Admin)", description = "Retrieves a paginated list of all users with optional filtering (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
        PagedResponse<UserResponse> pagedResponse = userService.getAllUsersAsResponse(pageParams);
        return ResponseEntity.ok(pagedResponse);
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Get all bookings (Admin)", description = "Retrieves a paginated list of all bookings with optional filtering (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of bookings"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> getAllBookings(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<BookingResponse> pagedResponse = bookingService.getAllBookingsWithFiltersAsResponse(
                    pageParams, null, null, status, null, null);
            return ResponseEntity.ok(pagedResponse);
        }
        
        var allBookings = bookingService.getAllBookingsAsResponse();
        return ResponseEntity.ok(allBookings);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Delete user (Admin)", description = "Deletes a user by ID (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bookings/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Delete booking (Admin)", description = "Deletes a booking by ID (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Booking deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
