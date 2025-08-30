package com.ticketing.repository;

import com.ticketing.model.Priority;
import com.ticketing.model.Status;
import com.ticketing.model.Ticket;
import com.ticketing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreator(User creator);
    List<Ticket> findByAssignee(User assignee);
    List<Ticket> findByStatus(Status status);
    List<Ticket> findByPriority(Priority priority);
    
    @Query("SELECT t FROM Ticket t WHERE t.creator = :user OR t.assignee = :user")
    List<Ticket> findByCreatorOrAssignee(@Param("user") User user);
    
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "(:creatorId IS NULL OR t.creator.id = :creatorId) AND " +
           "(LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Ticket> findTicketsWithFilters(
        @Param("status") Status status,
        @Param("priority") Priority priority,
        @Param("assigneeId") Long assigneeId,
        @Param("creatorId") Long creatorId,
        @Param("search") String search
    );
    
    @Query("SELECT t FROM Ticket t WHERE t.creator = :user AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Ticket> findUserTicketsWithFilters(
        @Param("user") User user,
        @Param("status") Status status,
        @Param("priority") Priority priority,
        @Param("search") String search
    );
}
