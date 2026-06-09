package stereovision.model;

import java.time.LocalDateTime;

public class UserAccount extends BaseEntity {
    private String username;
    private String passwordHash;
    private LocalDateTime createdAt;

    public UserAccount() {
        this.createdAt = LocalDateTime.now();
    }

    public UserAccount(int id, String username, String passwordHash, LocalDateTime createdAt) {
        super(id);
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
