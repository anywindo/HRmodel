package com.hr.dto.communications;

import model.communications.AnnouncementPriority;
import java.time.LocalDateTime;

public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private AnnouncementPriority priority;
    private String targetDepartmentId;
    private String targetDepartmentName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public AnnouncementPriority getPriority() { return priority; }
    public void setPriority(AnnouncementPriority priority) { this.priority = priority; }

    public String getTargetDepartmentId() { return targetDepartmentId; }
    public void setTargetDepartmentId(String targetDepartmentId) { this.targetDepartmentId = targetDepartmentId; }

    public String getTargetDepartmentName() { return targetDepartmentName; }
    public void setTargetDepartmentName(String targetDepartmentName) { this.targetDepartmentName = targetDepartmentName; }
}
