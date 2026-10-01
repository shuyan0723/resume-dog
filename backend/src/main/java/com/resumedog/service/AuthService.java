package com.resumedog.service;

import com.resumedog.common.BusinessException;
import com.resumedog.domain.User;
import com.resumedog.repository.UserRepository;
import com.resumedog.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public String register(String email, String password, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(40900, "该邮箱已注册");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname == null || nickname.isBlank() ? email.substring(0, email.indexOf('@')) : nickname);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);

        return jwtService.issue(user.getId(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(40100, "邮箱或密码不正确"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(40100, "邮箱或密码不正确");
        }
        return jwtService.issue(user.getId(), user.getEmail());
    }
}
