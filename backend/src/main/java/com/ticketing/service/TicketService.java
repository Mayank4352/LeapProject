package com.ticketing.service;

import com.ticketing.dto.TicketRequest;
import com.ticketing.model.*;
import com.ticketing.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EmailService emailService;

    public Ticket createTicket(TicketRequest ticketRequest, User creator) {
        Ticket ticket = new Ticket(
                ticketRequest.getSubject(),
                ticketRequest.getDescription(),
                ticketRequest.getPriority(),
                creator
        );

        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Send email notification
        emailService.sendTicketCreatedNotification(savedTicket);
        
        return savedTicket;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getUserTickets(User user) {
        return ticketRepository.findByCreator(user);
    }

    public List<Ticket> getAssignedTickets(User user) {
        return ticketRepository.findByAssignee(user);
    }

    public List<Ticket> getUserRelatedTickets(User user) {
        return ticketRepository.findByCreatorOrAssignee(user);
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public Ticket updateTicketStatus(Long id, Status status, User updatedBy) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    Status oldStatus = ticket.getStatus();
                    ticket.setStatus(status);
                    Ticket savedTicket = ticketRepository.save(ticket);
                    
                    // Send email notification for status change
                    if (!oldStatus.equals(status)) {
                        emailService.sendTicketStatusChangeNotification(savedTicket, oldStatus, status);
                    }
                    
                    return savedTicket;
                })
                .orElseThrow(() -> new RuntimeException("Ticket not found with id " + id));
    }

    public Ticket assignTicket(Long id, User assignee, User assignedBy) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    User oldAssignee = ticket.getAssignee();
                    ticket.setAssignee(assignee);
                    if (ticket.getStatus() == Status.OPEN) {
                        ticket.setStatus(Status.IN_PROGRESS);
                    }
                    Ticket savedTicket = ticketRepository.save(ticket);
                    
                    // Send email notification for assignment
                    emailService.sendTicketAssignmentNotification(savedTicket, oldAssignee, assignee);
                    
                    return savedTicket;
                })
                .orElseThrow(() -> new RuntimeException("Ticket not found with id " + id));
    }

    public Ticket rateTicket(Long id, Integer rating, String feedback, User user) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    if (!ticket.getCreator().equals(user)) {
                        throw new RuntimeException("Only ticket creator can rate the ticket");
                    }
                    if (ticket.getStatus() != Status.RESOLVED && ticket.getStatus() != Status.CLOSED) {
                        throw new RuntimeException("Can only rate resolved or closed tickets");
                    }
                    ticket.setRating(rating);
                    ticket.setFeedback(feedback);
                    return ticketRepository.save(ticket);
                })
                .orElseThrow(() -> new RuntimeException("Ticket not found with id " + id));
    }

    public List<Ticket> searchTickets(Status status, Priority priority, Long assigneeId, Long creatorId, String search) {
        return ticketRepository.findTicketsWithFilters(status, priority, assigneeId, creatorId, search != null ? search : "");
    }

    public List<Ticket> searchUserTickets(User user, Status status, Priority priority, String search) {
        return ticketRepository.findUserTicketsWithFilters(user, status, priority, search != null ? search : "");
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    public boolean canUserAccessTicket(User user, Ticket ticket) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.SUPPORT_AGENT) {
            // Support agents can only access tickets assigned to them
            return ticket.getAssignee() != null && ticket.getAssignee().equals(user);
        }
        return ticket.getCreator().equals(user);
    }

    public boolean canUserModifyTicket(User user, Ticket ticket) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.SUPPORT_AGENT) {
            return ticket.getAssignee() != null && ticket.getAssignee().equals(user);
        }
        return ticket.getCreator().equals(user);
    }
}
