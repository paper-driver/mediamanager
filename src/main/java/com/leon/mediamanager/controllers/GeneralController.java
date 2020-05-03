package com.leon.mediamanager.controllers;

import com.leon.mediamanager.models.ConfirmationToken;
import com.leon.mediamanager.models.ERole;
import com.leon.mediamanager.models.Role;
import com.leon.mediamanager.models.User;
import com.leon.mediamanager.payload.request.UpdateProfileRequest;
import com.leon.mediamanager.payload.response.ListResponse;
import com.leon.mediamanager.payload.response.MessageResponse;
import com.leon.mediamanager.payload.response.UpdateRequestResponse;
import com.leon.mediamanager.repository.ConfirmationTokenRepository;
import com.leon.mediamanager.repository.RoleRepository;
import com.leon.mediamanager.repository.UserRepository;
import com.leon.mediamanager.security.services.EmailSenderService;
import org.hibernate.sql.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.Valid;
import javax.xml.ws.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/protected")
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

    @Autowired
    PasswordEncoder encoder;

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
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        String roleUpdateMsg = "";
        String pwdUpdateMsg = "";

        logger.info("updating profile");
        logger.warn("update request of role: {}", updateProfileRequest.getRole());
        if(updateProfileRequest.getRole() != null && !updateProfileRequest.getRole().isEmpty()){

            /* organize requested roles */
            Set<Role> requestedRole = new HashSet<>();
            for(Role role: updateProfileRequest.getRole()){
                Role actualRole = roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                requestedRole.add(actualRole);
            }

            /* check whether there is change in roles */
            User user = userRepository.findByUsername(updateProfileRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            if(!user.getRoles().equals(requestedRole)){
                /* check existing of token */
                ConfirmationToken appendingToken = confirmationTokenRepository.findByUserId(user.getId());
                if(appendingToken != null){
                    /* there is a request already */
                    appendingToken.setRoles(requestedRole);
                }else{
                    /* create a new token */
                    appendingToken = new ConfirmationToken(user);
                    appendingToken.setRoles(requestedRole);
                }
                logger.warn("save token info: {}", appendingToken.getUser().getUsername());
                confirmationTokenRepository.saveAndFlush(appendingToken);

                /* send an email */
                logger.warn("generated token: {}", appendingToken.getToken());
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(emailAddress);
                mailMessage.setSubject("Media Manager Role update request from " + user.getUsername());
                mailMessage.setFrom(emailAddress);

                /* format the message body */
                String newline =System.getProperty("line.separator");
                StringBuffer sb = new StringBuffer();
                sb.append("To approve the request, please click following link :"
                        + "http://localhost:8080/api/public/roleconfirmation?token=" + appendingToken.getToken() + newline);
                sb.append("To reject the request, please click following link :"
                        + "http://localhost:8080/api/public/rolerejection?token=" + appendingToken.getToken());
                /* send message */
                mailMessage.setText(sb.toString());

                emailSenderService.sendEmail(mailMessage);
                roleUpdateMsg = "The request of updating role has been sent out to admin.";
            }
        }

        logger.warn("update request of password: {}", updateProfileRequest.getPassword());
        if(updateProfileRequest.getPassword() != null && !updateProfileRequest.getPassword().equals("")){
            User user = userRepository.findByUsername(updateProfileRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error: User is not found."));

            user.setPassword(encoder.encode(updateProfileRequest.getPassword()));
            userRepository.saveAndFlush(user);
            pwdUpdateMsg = "Password has been successfully updated.";
        }

        MessageResponse msgResponse = new MessageResponse(roleUpdateMsg + " " + pwdUpdateMsg);
        return ResponseEntity.ok(msgResponse);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingRequests() {
        /* Get all update requests */
        List<ConfirmationToken> requests = confirmationTokenRepository.findAll();
        List<UpdateRequestResponse> responses = new ArrayList<>();

        for(ConfirmationToken request : requests) {
            UpdateRequestResponse response = new UpdateRequestResponse();
            response.setObjectFromTokenObject(request);
            responses.add(response);
        }

        return ResponseEntity.ok(responses);
    }

}
