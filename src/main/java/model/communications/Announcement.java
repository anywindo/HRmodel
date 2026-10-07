package model.communications;

import jakarta.persistence.*;
import model.employee.Employee;
import model.department.Department;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Employee author;

    @Column(name = "system_author_name")
    private String systemAuthorName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_department_id")
    private Department targetDepartment;

    public Announcement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Employee getAuthor() { return author; }
    public void setAuthor(Employee author) { this.author = author; }

    public String getSystemAuthorName() { return systemAuthorName; }
    public void setSystemAuthorName(String systemAuthorName) { this.systemAuthorName = systemAuthorName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public AnnouncementPriority getPriority() { return priority; }
    public void setPriority(AnnouncementPriority priority) { this.priority = priority; }

    public Department getTargetDepartment() { return targetDepartment; }
    public void setTargetDepartment(Department targetDepartment) { this.targetDepartment = targetDepartment; }
}
