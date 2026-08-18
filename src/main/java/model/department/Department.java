package model.department;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "department_id", unique = true, nullable = false))
    private DepartmentId departmentId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_position_id")
    private model.position.Position headOfDepartment;

    protected Department() {
        // JPA requires default constructor
    }

    public Department(DepartmentId departmentId, String name, String description, model.position.Position headOfDepartment) {
        if (departmentId == null) {
            throw new IllegalArgumentException("Department ID is required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Department name is required.");
        }
        this.departmentId = departmentId;
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.headOfDepartment = headOfDepartment;
    }

    public Long getId() {
        return id;
    }

    public DepartmentId getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public model.position.Position getHeadOfDepartment() {
        return headOfDepartment;
    }

    public void updateDetails(String newName, String newDescription, model.position.Position newHeadOfDepartment) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Department name is required.");
        }
        this.name = newName.trim();
        this.description = newDescription != null ? newDescription.trim() : null;
        this.headOfDepartment = newHeadOfDepartment;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Department other)) return false;
        return departmentId.equals(other.departmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentId);
    }
}
