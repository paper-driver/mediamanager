package com.leon.mediamanager.repository;

import com.leon.mediamanager.models.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, String> {
    ConfirmationToken findByToken(String token);

    @Query(value = "SELECT u FROM ConfirmationToken u WHERE u.user.id = user_id")
    ConfirmationToken findByUserId(@Param("user_id") Long user_id);

    List<ConfirmationToken> findAll();
}
