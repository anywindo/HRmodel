package service.employee;

import com.hr.dto.EmployeeRequest;
import com.hr.dto.EmployeeResponse;
import model.employee.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.employee.EmployeeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
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
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        FullName fullName = new FullName(request.getFirstName(), request.getMiddleName(), request.getLastName());
        Email email = new Email(request.getEmail());
        PhoneNumber phone = new PhoneNumber(request.getPhoneNumber(), request.getPhoneCountryCode());
        Salary salary = new Salary(request.getSalaryAmount());
        
        Gender gender = Gender.valueOf(request.getGender().toUpperCase());
        Sex sex = Sex.valueOf(request.getSex().toUpperCase());

        // Validate via static factory in Domain Model
        Employee newEmployee = Employee.create(
                fullName, email, phone, 
                request.getDateOfBirth(), request.getHireDate(), 
                gender, sex, salary
        );

        Employee saved = employeeRepository.save(newEmployee);
        return new EmployeeResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(String employeeId, EmployeeRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

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

        employee.updateDetails(
            fullName, 
            phone, 
            gender, 
            sex, 
            status, 
            request.getDateOfBirth(), 
            request.getHireDate()
        );

        return new EmployeeResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        employee.terminate();
        employeeRepository.save(employee);
    }
}
