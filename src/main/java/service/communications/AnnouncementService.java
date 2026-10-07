package service.communications;

import com.hr.dto.communications.AnnouncementDTO;
import model.communications.Announcement;
import model.department.Department;
import model.employee.Employee;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.communications.AnnouncementRepository;
import repository.department.DepartmentRepository;
import repository.employee.EmployeeRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository, EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.announcementRepository = announcementRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<AnnouncementDTO> getAllAnnouncements(Long employeeId) {
        if (employeeId == null) {
            return announcementRepository.findAllByOrderByCreatedAtDesc()
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
        }
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        if (employee.getPosition() != null && employee.getPosition().getDepartment() != null) {
            return announcementRepository.findByTargetDepartmentIdOrTargetDepartmentIsNullOrderByCreatedAtDesc(employee.getPosition().getDepartment().getId())
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
        }
        return announcementRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public AnnouncementDTO createAnnouncement(AnnouncementDTO dto, Long authorId) {
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        
        if (authorId == null) {
            announcement.setSystemAuthorName("System Admin");
        } else {
            Employee author = employeeRepository.findById(authorId).orElseThrow();
            announcement.setAuthor(author);
        }
        
        announcement.setCreatedAt(LocalDateTime.now(ZoneId.of("GMT+7")));
        if (dto.getPriority() != null) {
            announcement.setPriority(dto.getPriority());
        }
        
        if (dto.getTargetDepartmentId() != null && !dto.getTargetDepartmentId().isEmpty()) {
            Department dept = departmentRepository.findByDepartmentIdValue(dto.getTargetDepartmentId()).orElseThrow();
            announcement.setTargetDepartment(dept);
        }

        Announcement saved = announcementRepository.save(announcement);
        return mapToDTO(saved);
    }
    
    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }

    private AnnouncementDTO mapToDTO(Announcement announcement) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        if (announcement.getAuthor() != null) {
            dto.setAuthorId(announcement.getAuthor().getId());
            dto.setAuthorName(announcement.getAuthor().getFullName().getFirstName() + " " + announcement.getAuthor().getFullName().getLastName());
        } else {
            dto.setAuthorName(announcement.getSystemAuthorName() != null ? announcement.getSystemAuthorName() : "Admin");
        }
        dto.setCreatedAt(announcement.getCreatedAt());
        dto.setPriority(announcement.getPriority());
        if (announcement.getTargetDepartment() != null) {
            dto.setTargetDepartmentId(announcement.getTargetDepartment().getDepartmentId().getValue());
            dto.setTargetDepartmentName(announcement.getTargetDepartment().getName());
        }
        return dto;
    }
}
