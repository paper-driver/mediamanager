package com.leon.mediamanager.controllers;

import com.leon.mediamanager.models.ConfirmationToken;
import com.leon.mediamanager.models.ERole;
import com.leon.mediamanager.models.Role;
import com.leon.mediamanager.models.User;
import com.leon.mediamanager.payload.request.UpdateProfileRequest;
import com.leon.mediamanager.payload.response.ListResponse;
import com.leon.mediamanager.payload.response.MessageResponse;
import com.leon.mediamanager.repository.ConfirmationTokenRepository;
import com.leon.mediamanager.repository.RoleRepository;
import com.leon.mediamanager.repository.UserRepository;
import com.leon.mediamanager.security.services.EmailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class GeneralController {

    private static final Logger logger = LoggerFactory.getLogger(GeneralController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    EmailSenderService emailSenderService;

    @Value("${email.address}")
    String emailAddress;

    @GetMapping("/roletypes")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getRoleTypes(){
        List<Role> roles = roleRepository.findAll();
        logger.info(roles.get(0).toString());

        return ResponseEntity.ok(new ListResponse<Role>(roles));
    }

    @PostMapping("/updateprofile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest updateProfileRequest){
        String roleUpdateMsg = "";
        String pwdUpdateMsg = "";

        if(updateProfileRequest.getRole() != null || !updateProfileRequest.getRole().isEmpty()){

            /* organize requested roles */
            List<Role> requestedRole = new ArrayList<Role>();
            for(ERole roleName: updateProfileRequest.getRole()){
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                requestedRole.add(role);
            }

            User user = userRepository.findByUsername(updateProfileRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            ConfirmationToken token = new ConfirmationToken(user, requestedRole);

            confirmationTokenRepository.saveAndFlush(token);

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(emailAddress);
            mailMessage.setSubject("Media Manager Role update request from " + user.getUsername());
            mailMessage.setFrom(emailAddress);
            mailMessage.setText("To approve the request, please click following link :"
                    + "http://localhost:8080/roleconfirmation?token=" + token.getToken());

            emailSenderService.sendEmail(mailMessage);
            roleUpdateMsg = "The request of updating role has been sent out to admin>";
        }

        if(updateProfileRequest.getPassword() != null || !updateProfileRequest.getPassword().isEmpty()){
            User user = userRepository.findByUsername(updateProfileRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            user.setPassword(updateProfileRequest.getPassword());
            userRepository.saveAndFlush(user);
            pwdUpdateMsg = "Password has been successfully updated.";
        }

        MessageResponse msgResponse = new MessageResponse(roleUpdateMsg + " " + pwdUpdateMsg);
        return ResponseEntity.ok(msgResponse);
    }
}
