package service.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hr.dto.audit.AuditLogEntry;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.HexFormat;

@Service
public class AuditLoggerService {

    private final BlockingQueue<AuditLogEntry> queue = new LinkedBlockingQueue<>(10000);
    private final ObjectMapper objectMapper;
    private final String LOG_DIR = "audit_logs";
    private Thread writerThread;
    private volatile boolean running = true;
    
    private String lastHash = "";
    private LocalDate currentLogDate;
    private BufferedWriter currentWriter;

    public AuditLoggerService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostConstruct
    public void init() {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        writerThread = new Thread(this::processQueue, "AuditLogWriterThread");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    @PreDestroy
    public void cleanup() {
        running = false;
        if (writerThread != null) {
            writerThread.interrupt();
        }
        closeWriter();
    }

    public void logEvent(AuditLogEntry entry) {
        // Non-blocking offer, if queue is full it drops the log to prevent OOM
        // In a real high-assurance system we might block, but user accepted some data loss
        if (!queue.offer(entry)) {
            System.err.println("Audit log queue is full, dropping event: " + entry.getId());
        }
    }

    private void processQueue() {
        while (running) {
            try {
                AuditLogEntry entry = queue.poll(1, TimeUnit.SECONDS);
                if (entry != null) {
                    processEntry(entry);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error processing audit log: " + e.getMessage());
            }
        }
        
        // Drain remaining on shutdown
        AuditLogEntry entry;
        while ((entry = queue.poll()) != null) {
            try {
                processEntry(entry);
            } catch (Exception e) {
                // Ignore errors during shutdown drain
            }
        }
    }

    private void processEntry(AuditLogEntry entry) throws Exception {
        LocalDate today = LocalDate.now();
        
        if (currentWriter == null || currentLogDate == null || !currentLogDate.equals(today)) {
            rollFile(today);
        }

        // Calculate hash
        entry.setPreviousHash(lastHash);
        entry.setContentForHashing(entry.getContentForHashing());
        String hashStr = calculateHash(entry.getContentForHashing());
        entry.setHash(hashStr);
        lastHash = hashStr;

        // Write to file
        String json = objectMapper.writeValueAsString(entry);
        currentWriter.write(json);
        currentWriter.newLine();
        currentWriter.flush(); // Flush immediately to minimize data loss on crash
    }

    private void rollFile(LocalDate today) throws IOException {
        closeWriter();
        currentLogDate = today;
        String filename = LOG_DIR + "/audit-" + today.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".jsonl";
        File file = new File(filename);
        
        boolean isNewFile = !file.exists();
        
        // If file exists, we should ideally read the last line to get the last hash.
        // For simplicity, if we restart, lastHash starts as empty string unless we implement reading the last line.
        // Reading the last line ensures chain continuity across restarts.
        if (!isNewFile && lastHash.isEmpty()) {
            lastHash = readLastHash(file);
        }
        
        currentWriter = new BufferedWriter(new FileWriter(file, true));
    }
    
    private String readLastHash(File file) {
        String lastLine = "";
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lastLine = line;
                }
            }
            if (!lastLine.isEmpty()) {
                AuditLogEntry entry = objectMapper.readValue(lastLine, AuditLogEntry.class);
                return entry.getHash();
            }
        } catch (Exception e) {
            System.err.println("Failed to read last hash: " + e.getMessage());
        }
        return "";
    }

    private void closeWriter() {
        if (currentWriter != null) {
            try {
                currentWriter.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    public static String calculateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
