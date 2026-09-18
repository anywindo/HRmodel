package service.payroll;

import com.hr.dto.payroll.PayrollSettingsDTO;
import com.hr.dto.payroll.TaxBracketDTO;
import model.payroll.PayrollSettings;
import model.payroll.TaxBracket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.payroll.PayrollSettingsRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PayrollSettingsService {

    private final PayrollSettingsRepository payrollSettingsRepository;

    public PayrollSettingsService(PayrollSettingsRepository payrollSettingsRepository) {
        this.payrollSettingsRepository = payrollSettingsRepository;
    }

    /**
     * Get current payroll settings. Creates defaults if none exist.
     */
    public PayrollSettingsDTO getSettings() {
        PayrollSettings settings = getSettingsEntity();
        return mapToDTO(settings);
    }

    /**
     * Get current payroll settings entity (internal use).
     */
    public PayrollSettings getSettingsEntity() {
        return payrollSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefaultSettings);
    }

    /**
     * Update payroll settings including tax brackets.
     * Tax brackets are replaced entirely (orphan removal handles cleanup).
     */
    public PayrollSettingsDTO updateSettings(PayrollSettingsDTO dto) {
        PayrollSettings settings = payrollSettingsRepository.findAll().stream()
                .findFirst()
                .orElse(new PayrollSettings());

        // Update scalar fields
        settings.setWorkingDaysPerMonth(dto.getWorkingDaysPerMonth());
        settings.setOvertimeMultiplierFirstHour(dto.getOvertimeMultiplierFirstHour());
        settings.setOvertimeMultiplierSubsequentHours(dto.getOvertimeMultiplierSubsequentHours());
        settings.setBpjsJhtEmployeeRate(dto.getBpjsJhtEmployeeRate());
        settings.setBpjsJpEmployeeRate(dto.getBpjsJpEmployeeRate());
        settings.setBpjsJhtEmployerRate(dto.getBpjsJhtEmployerRate());
        settings.setBpjsJpEmployerRate(dto.getBpjsJpEmployerRate());
        settings.setBpjsJkkRate(dto.getBpjsJkkRate());
        settings.setBpjsJkmRate(dto.getBpjsJkmRate());
        settings.setUpdatedAt(LocalDateTime.now());

        // Replace tax brackets (orphanRemoval = true handles deletions)
        settings.getTaxBrackets().clear();
        if (dto.getTaxBrackets() != null) {
            for (TaxBracketDTO bracketDTO : dto.getTaxBrackets()) {
                TaxBracket bracket = new TaxBracket(
                        bracketDTO.getMinAmount(),
                        bracketDTO.getMaxAmount(),
                        bracketDTO.getRate(),
                        settings
                );
                settings.getTaxBrackets().add(bracket);
            }
        }

        return mapToDTO(payrollSettingsRepository.save(settings));
    }

    /**
     * Create default settings with Indonesian PPh 21 tax brackets.
     */
    private PayrollSettings createDefaultSettings() {
        PayrollSettings settings = new PayrollSettings();
        
        // Add default PPh 21 progressive brackets
        List<TaxBracket> brackets = new ArrayList<>();
        brackets.add(new TaxBracket(BigDecimal.ZERO, new BigDecimal("60000000"), new BigDecimal("0.0500"), settings));
        brackets.add(new TaxBracket(new BigDecimal("60000001"), new BigDecimal("250000000"), new BigDecimal("0.1500"), settings));
        brackets.add(new TaxBracket(new BigDecimal("250000001"), new BigDecimal("500000000"), new BigDecimal("0.2500"), settings));
        brackets.add(new TaxBracket(new BigDecimal("500000001"), new BigDecimal("5000000000"), new BigDecimal("0.3000"), settings));
        brackets.add(new TaxBracket(new BigDecimal("5000000001"), null, new BigDecimal("0.3500"), settings));
        settings.setTaxBrackets(brackets);

        return payrollSettingsRepository.save(settings);
    }

    private PayrollSettingsDTO mapToDTO(PayrollSettings settings) {
        PayrollSettingsDTO dto = new PayrollSettingsDTO();
        dto.setId(settings.getId());
        dto.setWorkingDaysPerMonth(settings.getWorkingDaysPerMonth());
        dto.setOvertimeMultiplierFirstHour(settings.getOvertimeMultiplierFirstHour());
        dto.setOvertimeMultiplierSubsequentHours(settings.getOvertimeMultiplierSubsequentHours());
        dto.setBpjsJhtEmployeeRate(settings.getBpjsJhtEmployeeRate());
        dto.setBpjsJpEmployeeRate(settings.getBpjsJpEmployeeRate());
        dto.setBpjsJhtEmployerRate(settings.getBpjsJhtEmployerRate());
        dto.setBpjsJpEmployerRate(settings.getBpjsJpEmployerRate());
        dto.setBpjsJkkRate(settings.getBpjsJkkRate());
        dto.setBpjsJkmRate(settings.getBpjsJkmRate());

        List<TaxBracketDTO> bracketDTOs = settings.getTaxBrackets().stream()
                .map(b -> new TaxBracketDTO(b.getId(), b.getMinAmount(), b.getMaxAmount(), b.getRate()))
                .collect(Collectors.toList());
        dto.setTaxBrackets(bracketDTOs);

        return dto;
    }
}
