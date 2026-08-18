package model.position;

import jakarta.persistence.*;
import model.department.Department;

@Entity
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "position_id", unique = true, nullable = false))
    private PositionId positionId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reports_to_id")
    private Position reportsTo;

    // JPA requires no-arg constructor
    protected Position() {}

    public Position(PositionId positionId, String title, String description, Department department, Position reportsTo) {
        if (positionId == null) throw new IllegalArgumentException("Position ID is required");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (department == null) throw new IllegalArgumentException("Department is required");
        
        // Prevent immediate self-cycle
        if (reportsTo != null && this.positionId != null && this.positionId.equals(reportsTo.getPositionId())) {
            throw new IllegalArgumentException("A position cannot report to itself");
        }

        this.positionId = positionId;
        this.title = title;
        this.description = description;
        this.department = department;
        this.reportsTo = reportsTo;
    }

    public void updateDetails(String newTitle, String newDescription, Department newDepartment, Position newReportsTo) {
        if (newTitle == null || newTitle.isBlank()) throw new IllegalArgumentException("Title is required");
        if (newDepartment == null) throw new IllegalArgumentException("Department is required");
        
        if (newReportsTo != null && this.positionId.equals(newReportsTo.getPositionId())) {
            throw new IllegalArgumentException("A position cannot report to itself");
        }

        this.title = newTitle;
        this.description = newDescription;
        this.department = newDepartment;
        this.reportsTo = newReportsTo;
    }

    public Long getId() {
        return id;
    }

    public PositionId getPositionId() {
        return positionId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Department getDepartment() {
        return department;
    }

    public Position getReportsTo() {
        return reportsTo;
    }
}
