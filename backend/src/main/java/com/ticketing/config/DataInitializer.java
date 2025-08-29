package com.ticketing.config;

import com.ticketing.model.Role;
import com.ticketing.model.User;
import com.ticketing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@ticketdesk.com");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin user created: admin / password123");
        }

        // Create default support agent if not exists
        if (!userRepository.existsByUsername("support")) {
            User support = new User();
            support.setUsername("support");
            support.setEmail("support@ticketdesk.com");
            support.setPassword(passwordEncoder.encode("password123"));
            support.setFirstName("Support");
            support.setLastName("Agent");
            support.setRole(Role.SUPPORT_AGENT);
            userRepository.save(support);
            System.out.println("Default support user created: support / password123");
        }

        // Create default regular user if not exists
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@ticketdesk.com");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setFirstName("Demo");
            user.setLastName("User");
            user.setRole(Role.USER);
            userRepository.save(user);
            System.out.println("Default regular user created: user / password123");
        }
    }
}
