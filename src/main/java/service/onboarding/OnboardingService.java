package service.onboarding;

import com.hr.dto.onboarding.EmployeeTaskDTO;
import com.hr.dto.onboarding.OnboardingTemplateDTO;
import com.hr.dto.onboarding.TemplateTaskDTO;
import model.employee.Employee;
import model.onboarding.EmployeeTask;
import model.onboarding.OnboardingTemplate;
import model.onboarding.TemplateTask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.employee.EmployeeRepository;
import repository.onboarding.EmployeeTaskRepository;
import repository.onboarding.OnboardingTemplateRepository;
import repository.onboarding.TemplateTaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OnboardingService {

    private final OnboardingTemplateRepository templateRepository;
    private final TemplateTaskRepository templateTaskRepository;
    private final EmployeeTaskRepository employeeTaskRepository;
    private final EmployeeRepository employeeRepository;

    public OnboardingService(OnboardingTemplateRepository templateRepository,
                             TemplateTaskRepository templateTaskRepository,
                             EmployeeTaskRepository employeeTaskRepository,
                             EmployeeRepository employeeRepository) {
        this.templateRepository = templateRepository;
        this.templateTaskRepository = templateTaskRepository;
        this.employeeTaskRepository = employeeTaskRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<OnboardingTemplateDTO> getAllTemplates() {
        return templateRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public OnboardingTemplateDTO createTemplate(OnboardingTemplateDTO dto) {
        OnboardingTemplate template = new OnboardingTemplate();
        template.setName(dto.getName());
        template.setType(dto.getType());
        template.setDescription(dto.getDescription());
        template = templateRepository.save(template);

        if (dto.getTasks() != null) {
            for (TemplateTaskDTO taskDto : dto.getTasks()) {
                TemplateTask task = new TemplateTask();
                task.setTemplate(template);
                task.setTitle(taskDto.getTitle());
                task.setDescription(taskDto.getDescription());
                task.setAssignedRole(taskDto.getAssignedRole());
                template.getTasks().add(task);
            }
            templateRepository.save(template);
        }

        return mapToDTO(template);
    }

    public void assignTemplateToEmployee(Long templateId, Long employeeId) {
        OnboardingTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        for (TemplateTask templateTask : template.getTasks()) {
            EmployeeTask employeeTask = new EmployeeTask();
            employeeTask.setEmployee(employee);
            employeeTask.setTemplateTask(templateTask);
            employeeTask.setStatus("PENDING");
            employeeTaskRepository.save(employeeTask);
        }
    }

    public List<EmployeeTaskDTO> getTasksForEmployee(Long employeeId) {
        return employeeTaskRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapEmployeeTaskToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeTaskDTO> getTasksByRole(String role) {
        return employeeTaskRepository.findByAssignedRole(role).stream()
                .map(this::mapEmployeeTaskToDTO)
                .collect(Collectors.toList());
    }

    public void completeTask(Long employeeTaskId, Long userId) {
        EmployeeTask task = employeeTaskRepository.findById(employeeTaskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletedById(userId);
        employeeTaskRepository.save(task);
    }

    private OnboardingTemplateDTO mapToDTO(OnboardingTemplate template) {
        OnboardingTemplateDTO dto = new OnboardingTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setType(template.getType());
        dto.setDescription(template.getDescription());
        dto.setTasks(template.getTasks().stream().map(this::mapTaskToDTO).collect(Collectors.toList()));
        return dto;
    }

    private TemplateTaskDTO mapTaskToDTO(TemplateTask task) {
        TemplateTaskDTO dto = new TemplateTaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setAssignedRole(task.getAssignedRole());
        return dto;
    }

    private EmployeeTaskDTO mapEmployeeTaskToDTO(EmployeeTask task) {
        EmployeeTaskDTO dto = new EmployeeTaskDTO();
        dto.setId(task.getId());
        dto.setEmployeeId(task.getEmployee().getId());
        dto.setEmployeeName(task.getEmployee().getFullName().getFirstName() + " " + task.getEmployee().getFullName().getLastName());
        dto.setTemplateTaskId(task.getTemplateTask().getId());
        dto.setTaskTitle(task.getTemplateTask().getTitle());
        dto.setTaskDescription(task.getTemplateTask().getDescription());
        dto.setAssignedRole(task.getTemplateTask().getAssignedRole());
        dto.setStatus(task.getStatus());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCompletedById(task.getCompletedById());
        return dto;
    }
}
