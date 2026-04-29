package stereovision.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public final class AuditService {
    private static AuditService instance;
    private final String auditPath;

    private AuditService() {
        this.auditPath = System.getProperty("stereovision.audit.path", "audit.csv");
        ensureHeader();
    }

    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public synchronized void logAction(String actionName) {
        File file = new File(auditPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            writer.println(actionName + "," + LocalDateTime.now());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write to the audit CSV file.", exception);
        }
    }

    private void ensureHeader() {
        File file = new File(auditPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (file.exists() && file.length() > 0) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            writer.println("nume_actiune,timestamp");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not initialize the audit CSV file.", exception);
        }
    }
}
