package com.leon.mediamanager.controllers;

import com.leon.mediamanager.models.ERole;
import com.leon.mediamanager.models.Role;
import com.leon.mediamanager.payload.response.ListResponse;
import com.leon.mediamanager.repository.RoleRepository;
import com.leon.mediamanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class GeneralController {

    private static final Logger logger = LoggerFactory.getLogger(GeneralController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @GetMapping("/roletypes")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getRoleTypes(){
        List<Role> roles = roleRepository.findAll();
        logger.info(roles.get(0).toString());

        return ResponseEntity.ok(new ListResponse<Role>(roles));
    }
}
