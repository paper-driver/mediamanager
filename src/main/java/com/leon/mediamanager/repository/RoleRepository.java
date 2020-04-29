package com.leon.mediamanager.repository;

import java.util.Optional;

import com.leon.mediamanager.models.ERole;
import com.leon.mediamanager.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
