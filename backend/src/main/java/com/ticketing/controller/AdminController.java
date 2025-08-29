package com.ticketing.controller;

import com.ticketing.dto.MessageResponse;
import com.ticketing.dto.SignupRequest;
import com.ticketing.model.Role;
import com.ticketing.model.Ticket;
import com.ticketing.model.User;
import com.ticketing.service.TicketService;
import com.ticketing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = userService.createUser(signUpRequest);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        try {
            User updatedUser = userService.updateUser(id, userUpdate);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }

    @GetMapping("/users/support-agents")
    public ResponseEntity<List<User>> getSupportAgents() {
        List<User> supportAgents = userService.getActiveSupportAgents();
        return ResponseEntity.ok(supportAgents);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/tickets/{id}/force-assign")
    public ResponseEntity<?> forceAssignTicket(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Long assigneeId = request.get("assigneeId");
        if (assigneeId == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Assignee ID is required"));
        }

        Optional<User> assigneeOpt = userService.getUserById(assigneeId);
        if (assigneeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Assignee not found"));
        }

        User assignee = assigneeOpt.get();
        if (assignee.getRole() != Role.SUPPORT_AGENT && assignee.getRole() != Role.ADMIN) {
            return ResponseEntity.badRequest().body(new MessageResponse("Can only assign to support agents or admins"));
        }

        // Admin can force assign any ticket
        User admin = userService.getUserById(1L).orElse(null); // Assuming admin user for audit
        Ticket updatedTicket = ticketService.assignTicket(id, assignee, admin);
        return ResponseEntity.ok(updatedTicket);
    }

    @PutMapping("/tickets/{id}/force-status")
    public ResponseEntity<?> forceUpdateTicketStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String statusStr = request.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Status is required"));
            }

            com.ticketing.model.Status status = com.ticketing.model.Status.valueOf(statusStr);
            User admin = userService.getUserById(1L).orElse(null); // Assuming admin user for audit
            Ticket updatedTicket = ticketService.updateTicketStatus(id, status, admin);
            return ResponseEntity.ok(updatedTicket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid status"));
        }
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(new MessageResponse("Ticket deleted successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        List<User> allUsers = userService.getAllUsers();
        List<Ticket> allTickets = ticketService.getAllTickets();
        
        long userCount = allUsers.size();
        long adminCount = allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count();
        long supportAgentCount = allUsers.stream().filter(u -> u.getRole() == Role.SUPPORT_AGENT).count();
        long regularUserCount = allUsers.stream().filter(u -> u.getRole() == Role.USER).count();
        
        long totalTickets = allTickets.size();
        long openTickets = allTickets.stream().filter(t -> t.getStatus() == com.ticketing.model.Status.OPEN).count();
        long inProgressTickets = allTickets.stream().filter(t -> t.getStatus() == com.ticketing.model.Status.IN_PROGRESS).count();
        long resolvedTickets = allTickets.stream().filter(t -> t.getStatus() == com.ticketing.model.Status.RESOLVED).count();
        long closedTickets = allTickets.stream().filter(t -> t.getStatus() == com.ticketing.model.Status.CLOSED).count();
        
        Map<String, Object> stats = Map.of(
            "users", Map.of(
                "total", userCount,
                "admins", adminCount,
                "supportAgents", supportAgentCount,
                "regularUsers", regularUserCount
            ),
            "tickets", Map.of(
                "total", totalTickets,
                "open", openTickets,
                "inProgress", inProgressTickets,
                "resolved", resolvedTickets,
                "closed", closedTickets
            )
        );
        
        return ResponseEntity.ok(stats);
    }
}
