package com.hellFire.Real_Time_Notifications_System.services;

import com.hellFire.Real_Time_Notifications_System.dtos.request.CreateUserRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.response.UserResponse;
import com.hellFire.Real_Time_Notifications_System.exceptions.AppException;
import com.hellFire.Real_Time_Notifications_System.mapper.UserMapper;
import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import com.hellFire.Real_Time_Notifications_System.models.EmailVerificationToken;
import com.hellFire.Real_Time_Notifications_System.repositories.IEmailVerificationTokenRepository;
import com.hellFire.Real_Time_Notifications_System.repositories.IUserRepository;
import com.hellFire.Real_Time_Notifications_System.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final IUserRepository repo;
    private final IEmailVerificationTokenRepository emailVerificationTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final String clientBaseUrl;

    public AuthService(
            IUserRepository repo,
            IEmailVerificationTokenRepository emailVerificationTokenRepository,
            JwtUtil jwtUtil,
            PasswordEncoder encoder,
            UserMapper userMapper,
            EmailService emailService,
            @Value("${app.client.base-url:http://localhost:3000}") String clientBaseUrl
    ) {
        this.repo = repo;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.clientBaseUrl = clientBaseUrl;
    }

    public String register(CreateUserRequest request) {
        Optional<AppUsers> appUsersOptional = repo.findByUsername(request.getUsername());
        if (appUsersOptional.isPresent()){
            throw new AppException("USERNAME_TAKEN", "Username already taken");
        }
        if (repo.existsByEmail(request.getEmail())) {
            throw new AppException("EMAIL_TAKEN", "Email already registered");
        }
        request.setPassword(encoder.encode(request.getPassword()));
        AppUsers user = repo.save(userMapper.toEntity(request));
        createAndSendVerification(user);

        return "Account created. Please verify your email before login.";
    }

    public UserResponse login(String email, String password) {
        AppUsers user = repo.findByEmail(email)
                .orElseThrow(() -> new AppException("INVALID_CREDENTIALS", "Invalid credentials"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new AppException("INVALID_CREDENTIALS", "Invalid credentials");
        }
        if (!user.isEmailVerified()) {
            throw new AppException("EMAIL_NOT_VERIFIED", "Please verify your email before login");
        }
        return createUserResponse(user);
    }

    public String verifyEmail(String rawToken) {
        String normalizedToken = rawToken == null ? "" : rawToken.trim();
        String tokenHash = sha256(normalizedToken);
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByTokenHashAndUsedAtIsNull(tokenHash)
                .orElseThrow(() -> new AppException("INVALID_VERIFICATION_TOKEN", "Invalid or expired verification link"));

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException("INVALID_VERIFICATION_TOKEN", "Invalid or expired verification link");
        }

        AppUsers user = repo.findById(verificationToken.getUserId())
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found"));

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(Instant.now());
            repo.save(user);
        }

        verificationToken.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(verificationToken);

        return "Email verified successfully. You can now sign in.";
    }

    public String resendVerificationEmail(String email) {
        AppUsers user = repo.findByEmail(email)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "No account found for this email"));

        if (user.isEmailVerified()) {
            return "Email is already verified. Please sign in.";
        }

        createAndSendVerification(user);
        return "Verification email sent.";
    }

    private UserResponse createUserResponse(AppUsers appUsers){
        UserResponse userResponse = new UserResponse();
        userResponse.setUser(userMapper.toDto(appUsers));
        userResponse.setToken(jwtUtil.generateToken(appUsers));
        return userResponse;
    }

    public List<AppUsers> searchUsers(String query) {
        return repo.findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(query, query);
    }

    private void createAndSendVerification(AppUsers user) {
        String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();
        String hashedToken = sha256(rawToken);

        emailVerificationTokenRepository.deleteByUserId(user.getId());

        EmailVerificationToken emailToken = new EmailVerificationToken();
        emailToken.setUserId(user.getId());
        emailToken.setTokenHash(hashedToken);
        emailToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        emailVerificationTokenRepository.save(emailToken);

        String verifyUrl = clientBaseUrl + "/auth/verify-email?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        emailService.sendEmailVerification(user.getEmail(), verifyUrl);
    }

    private String sha256(String rawValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new AppException("INTERNAL_SERVER_ERROR", "Failed to create verification token");
        }
    }
}