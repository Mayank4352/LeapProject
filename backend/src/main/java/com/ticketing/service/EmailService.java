package com.ticketing.service;

import com.ticketing.model.Comment;
import com.ticketing.model.Status;
import com.ticketing.model.Ticket;
import com.ticketing.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendTicketCreatedNotification(Ticket ticket) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCreator().getEmail());
            message.setSubject("Ticket Created - #" + ticket.getId());
            message.setText("Your ticket has been created successfully.\n\n" +
                    "Ticket ID: #" + ticket.getId() + "\n" +
                    "Subject: " + ticket.getSubject() + "\n" +
                    "Priority: " + ticket.getPriority() + "\n" +
                    "Status: " + ticket.getStatus() + "\n\n" +
                    "We will get back to you soon.");
            
            mailSender.send(message);
            logger.info("Ticket created notification sent to: {}", ticket.getCreator().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send ticket created notification: {}", e.getMessage());
        }
    }

    public void sendTicketStatusChangeNotification(Ticket ticket, Status oldStatus, Status newStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCreator().getEmail());
            message.setSubject("Ticket Status Updated - #" + ticket.getId());
            message.setText("Your ticket status has been updated.\n\n" +
                    "Ticket ID: #" + ticket.getId() + "\n" +
                    "Subject: " + ticket.getSubject() + "\n" +
                    "Previous Status: " + oldStatus + "\n" +
                    "New Status: " + newStatus + "\n\n" +
                    "Thank you for your patience.");
            
            mailSender.send(message);
            logger.info("Ticket status change notification sent to: {}", ticket.getCreator().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send ticket status change notification: {}", e.getMessage());
        }
    }

    public void sendTicketAssignmentNotification(Ticket ticket, User oldAssignee, User newAssignee) {
        try {
            if (newAssignee != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(newAssignee.getEmail());
                message.setSubject("Ticket Assigned - #" + ticket.getId());
                message.setText("A ticket has been assigned to you.\n\n" +
                        "Ticket ID: #" + ticket.getId() + "\n" +
                        "Subject: " + ticket.getSubject() + "\n" +
                        "Priority: " + ticket.getPriority() + "\n" +
                        "Status: " + ticket.getStatus() + "\n\n" +
                        "Please review and take action as needed.");
                
                mailSender.send(message);
                logger.info("Ticket assignment notification sent to: {}", newAssignee.getEmail());
            }
        } catch (Exception e) {
            logger.error("Failed to send ticket assignment notification: {}", e.getMessage());
        }
    }

    public void sendCommentAddedNotification(Comment comment) {
        try {
            Ticket ticket = comment.getTicket();
            String recipientEmail = ticket.getCreator().getEmail();
            
            // Don't send notification to the comment author
            if (!comment.getAuthor().getEmail().equals(recipientEmail)) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(recipientEmail);
                message.setSubject("New Comment on Ticket #" + ticket.getId());
                message.setText("A new comment has been added to your ticket.\n\n" +
                        "Ticket ID: #" + ticket.getId() + "\n" +
                        "Subject: " + ticket.getSubject() + "\n" +
                        "Comment by: " + comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName() + "\n" +
                        "Comment: " + comment.getContent() + "\n\n" +
                        "Please check your ticket for more details.");
                
                mailSender.send(message);
                logger.info("Comment notification sent to: {}", recipientEmail);
            }
        } catch (Exception e) {
            logger.error("Failed to send comment notification: {}", e.getMessage());
        }
    }
}
