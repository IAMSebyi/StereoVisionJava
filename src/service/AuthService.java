package stereovision.service;

import stereovision.config.DatabaseInitializer;
import stereovision.model.UserAccount;
import stereovision.repository.UserRepository;
import stereovision.util.PasswordUtils;

import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AuthService() {
        DatabaseInitializer.initialize();
        this.userRepository = new UserRepository();
        this.auditService = AuditService.getInstance();
    }

    public UserAccount register(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        validatePassword(password);

        Optional<UserAccount> existingUser = userRepository.readByUsername(normalizedUsername);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("A user with this username already exists.");
        }

        UserAccount user = new UserAccount(0, normalizedUsername, PasswordUtils.hashPassword(password), LocalDateTime.now());
        userRepository.create(user);
        auditService.logAction("register_user");
        return user;
    }

    public UserAccount login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        validatePassword(password);

        UserAccount user = userRepository.readByUsername(normalizedUsername)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!PasswordUtils.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        auditService.logAction("login_user");
        return user;
    }

    private String normalizeUsername(String username) {
        String normalized = username == null ? "" : username.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (normalized.length() < 3) {
            throw new IllegalArgumentException("Username must have at least 3 characters.");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password.length() < 4) {
            throw new IllegalArgumentException("Password must have at least 4 characters.");
        }
    }
}
