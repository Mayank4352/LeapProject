package com.ticketing.controller;

import com.ticketing.dto.CommentRequest;
import com.ticketing.dto.MessageResponse;
import com.ticketing.dto.TicketRequest;
import com.ticketing.model.*;
import com.ticketing.service.CommentService;
import com.ticketing.service.TicketService;
import com.ticketing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    @Autowired
    private TicketService ticketService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createTicket(@Valid @RequestBody TicketRequest ticketRequest, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Ticket ticket = ticketService.createTicket(ticketRequest, user);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getUserTickets(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Ticket> tickets;
        
        if (user.getRole() == Role.ADMIN) {
            tickets = ticketService.getAllTickets();
        } else if (user.getRole() == Role.SUPPORT_AGENT) {
            tickets = ticketService.getUserRelatedTickets(user);
        } else {
            tickets = ticketService.getUserTickets(user);
        }
        
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTicket(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Ticket ticket = ticketOpt.get();
        if (!ticketService.canUserAccessTicket(user, ticket)) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }
        
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_AGENT')")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Ticket ticket = ticketOpt.get();
        if (user.getRole() != Role.ADMIN && !ticketService.canUserModifyTicket(user, ticket)) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }
        
        try {
            Status status = Status.valueOf(request.get("status"));
            Ticket updatedTicket = ticketService.updateTicketStatus(id, status, user);
            return ResponseEntity.ok(updatedTicket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid status"));
        }
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_AGENT')")
    public ResponseEntity<?> assignTicket(@PathVariable Long id, @RequestBody Map<String, Long> request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long assigneeId = request.get("assigneeId");
        Optional<User> assigneeOpt = userService.getUserById(assigneeId);
        
        if (assigneeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Assignee not found"));
        }
        
        User assignee = assigneeOpt.get();
        if (assignee.getRole() != Role.SUPPORT_AGENT && assignee.getRole() != Role.ADMIN) {
            return ResponseEntity.badRequest().body(new MessageResponse("Can only assign to support agents or admins"));
        }
        
        Ticket updatedTicket = ticketService.assignTicket(id, assignee, user);
        return ResponseEntity.ok(updatedTicket);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest commentRequest, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Ticket ticket = ticketOpt.get();
        if (!ticketService.canUserAccessTicket(user, ticket)) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }
        
        Comment comment = commentService.addComment(commentRequest, ticket, user);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getTicketComments(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Ticket ticket = ticketOpt.get();
        if (!ticketService.canUserAccessTicket(user, ticket)) {
            return ResponseEntity.status(403).body(new MessageResponse("Access denied"));
        }
        
        List<Comment> comments = commentService.getTicketComments(ticket);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateTicket(@PathVariable Long id, @RequestBody Map<String, Object> request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        try {
            Integer rating = (Integer) request.get("rating");
            String feedback = (String) request.get("feedback");
            
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(new MessageResponse("Rating must be between 1 and 5"));
            }
            
            Ticket updatedTicket = ticketService.rateTicket(id, rating, feedback, user);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Ticket>> searchTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        Status statusEnum = null;
        Priority priorityEnum = null;
        
        try {
            if (status != null) statusEnum = Status.valueOf(status);
            if (priority != null) priorityEnum = Priority.valueOf(priority);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Ticket> tickets;
        if (user.getRole() == Role.ADMIN) {
            tickets = ticketService.searchTickets(statusEnum, priorityEnum, assigneeId, creatorId, search);
        } else {
            tickets = ticketService.searchUserTickets(user, statusEnum, priorityEnum, search);
        }
        
        return ResponseEntity.ok(tickets);
    }
}
