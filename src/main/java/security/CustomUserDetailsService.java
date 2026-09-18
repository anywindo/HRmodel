package security;

import model.auth.Permission;
import model.auth.Role;
import model.auth.SystemAdmin;
import model.employee.Employee;
import model.employee.EmployeeStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.auth.SystemAdminRepository;
import repository.employee.EmployeeRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final SystemAdminRepository systemAdminRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository, SystemAdminRepository systemAdminRepository) {
        this.employeeRepository = employeeRepository;
        this.systemAdminRepository = systemAdminRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First check if user is a SystemAdmin
        Optional<SystemAdmin> adminOpt = systemAdminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            SystemAdmin admin = adminOpt.get();
            Set<GrantedAuthority> authorities = new HashSet<>();
            for (Role role : admin.getRoles()) {
                String roleName = role.getName();
                String roleAuth = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                authorities.add(new SimpleGrantedAuthority(roleAuth));
                for (Permission permission : role.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                }
            }
            return new org.springframework.security.core.userdetails.User(
                    admin.getEmail(),
                    admin.getPasswordHash(),
                    authorities
            );
        }

        // Fallback to Employee
        Employee employee = employeeRepository.findByEmail_Value(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new DisabledException("Employee account has been terminated");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        
        for (Role role : employee.getRoles()) {
            String roleName = role.getName();
            String roleAuth = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            authorities.add(new SimpleGrantedAuthority(roleAuth));
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                employee.getEmail().getValue(),
                employee.getPasswordHash(),
                authorities
        );
    }
}
