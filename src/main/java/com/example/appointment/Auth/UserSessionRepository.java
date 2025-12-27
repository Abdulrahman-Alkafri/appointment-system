package com.example.appointment.Auth;

import com.example.appointment.User.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionModel, Long> {

    Optional<UserSessionModel> findByRefreshToken(String refreshToken);

    List<UserSessionModel> findAllByUserId(Long userId);

    void deleteByRefreshToken(String refreshToken);

    void deleteByUser(UserModel user);

    void deleteByUserId(Long userId);
}
