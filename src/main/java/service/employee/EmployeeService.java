package service.employee;

import com.hr.dto.EmployeeRequest;
import com.hr.dto.EmployeeResponse;
import model.employee.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.position.PositionRepository;
import repository.employee.EmployeeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;

    public EmployeeService(EmployeeRepository employeeRepository, PositionRepository positionRepository) {
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::new)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getEmployeesByDepartmentId(String departmentId) {
        return employeeRepository.findByPosition_Department_DepartmentIdValue(departmentId.toUpperCase()).stream()
                .map(EmployeeResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return new EmployeeResponse(employee);
    }

    @Transactional
    @SuppressWarnings("null")
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        FullName fullName = new FullName(request.getFirstName(), request.getMiddleName(), request.getLastName());
        Email email = new Email(request.getEmail());
        PhoneNumber phone = new PhoneNumber(request.getPhoneNumber(), request.getPhoneCountryCode());
        Salary salary = new Salary(request.getSalaryAmount());
        
        Gender gender = Gender.valueOf(request.getGender().toUpperCase());
        Sex sex = Sex.valueOf(request.getSex().toUpperCase());
        MaritalStatus maritalStatus = request.getMaritalStatus() != null ? MaritalStatus.valueOf(request.getMaritalStatus().toUpperCase()) : MaritalStatus.PREFER_NOT_TO_SAY;

        model.position.Position position = null;
        if (request.getPositionId() != null && !request.getPositionId().isBlank()) {
            position = positionRepository.findByPositionId(new model.position.PositionId(request.getPositionId().toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Position not found"));
        }

        // Validate via static factory in Domain Model
        Employee newEmployee = Employee.create(
                fullName, email, phone, 
                request.getDateOfBirth(), request.getHireDate(), 
                gender, sex, salary, maritalStatus, position
        );

        Employee saved = employeeRepository.save(newEmployee);
        return new EmployeeResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(String employeeId, EmployeeRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new IllegalStateException("Cannot edit a terminated employee");
        }

        if (request.getEmail() != null) {
            employee.changeEmail(new Email(request.getEmail()));
        }
        if (request.getSalaryAmount() != null) {
            employee.updateSalary(new Salary(request.getSalaryAmount()));
        }
        
        FullName fullName = null;
        if (request.getFirstName() != null && request.getLastName() != null) {
            fullName = new FullName(request.getFirstName(), request.getMiddleName(), request.getLastName());
        }
        
        PhoneNumber phone = null;
        if (request.getPhoneNumber() != null && request.getPhoneCountryCode() != null) {
            phone = new PhoneNumber(request.getPhoneNumber(), request.getPhoneCountryCode());
        }
        
        Gender gender = request.getGender() != null ? Gender.valueOf(request.getGender().toUpperCase()) : null;
        Sex sex = request.getSex() != null ? Sex.valueOf(request.getSex().toUpperCase()) : null;
        EmployeeStatus status = request.getStatus() != null ? EmployeeStatus.valueOf(request.getStatus().toUpperCase()) : null;
        MaritalStatus maritalStatus = request.getMaritalStatus() != null ? MaritalStatus.valueOf(request.getMaritalStatus().toUpperCase()) : null;

        model.position.Position position = null;
        if (request.getPositionId() != null && !request.getPositionId().isBlank()) {
            position = positionRepository.findByPositionId(new model.position.PositionId(request.getPositionId().toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Position not found"));
        }

        employee.updateDetails(
            fullName, 
            phone, 
            gender, 
            sex, 
            status, 
            request.getDateOfBirth(), 
            request.getHireDate(),
            maritalStatus,
            position
        );

        return new EmployeeResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        
        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new IllegalStateException("Employee is already terminated");
        }
        
        employee.terminate();
        employeeRepository.save(employee);
    }
}
