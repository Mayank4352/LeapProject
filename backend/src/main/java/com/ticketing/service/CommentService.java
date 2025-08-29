package com.ticketing.service;

import com.ticketing.dto.CommentRequest;
import com.ticketing.model.Comment;
import com.ticketing.model.Ticket;
import com.ticketing.model.User;
import com.ticketing.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EmailService emailService;

    public Comment addComment(CommentRequest commentRequest, Ticket ticket, User author) {
        Comment comment = new Comment(commentRequest.getContent(), ticket, author);
        Comment savedComment = commentRepository.save(comment);
        
        // Send email notification for new comment
        emailService.sendCommentAddedNotification(savedComment);
        
        return savedComment;
    }

    public List<Comment> getTicketComments(Ticket ticket) {
        return commentRepository.findByTicketOrderByCreatedAtAsc(ticket);
    }
}
