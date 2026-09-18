package service.payroll;

import model.employee.Employee;
import model.payroll.PayrollItem;
import model.payroll.PayrollSettings;
import model.payroll.TaxBracket;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PayrollCalculationService {

    private final PayrollSettingsService settingsService;

    public PayrollCalculationService(PayrollSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void calculateTaxesAndDeductions(PayrollItem item) {
        PayrollSettings settings = settingsService.getSettingsEntity();
        
        BigDecimal grossPay = item.getBaseSalary().add(item.getOvertimePay()).add(item.getAllowances());
        
        // 1. Calculate BPJS Kesehatan
        BigDecimal bpjsKesehatanBase = grossPay.min(settings.getBpjsKesehatanMaxSalary());
        item.setBpjsKesehatanEmployee(bpjsKesehatanBase.multiply(settings.getBpjsKesehatanEmployeeRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        item.setBpjsKesehatanCompany(bpjsKesehatanBase.multiply(settings.getBpjsKesehatanCompanyRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));

        // 2. Calculate BPJS Ketenagakerjaan (JHT + JP + JKK + JKM)
        BigDecimal jhtEmployee = grossPay.multiply(settings.getBpjsJhtEmployeeRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal jhtCompany = grossPay.multiply(settings.getBpjsJhtEmployerRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal jpBase = grossPay.min(settings.getBpjsJpMaxSalary());
        BigDecimal jpEmployee = jpBase.multiply(settings.getBpjsJpEmployeeRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal jpCompany = jpBase.multiply(settings.getBpjsJpEmployerRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal jkkCompany = grossPay.multiply(settings.getBpjsJkkRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal jkmCompany = grossPay.multiply(settings.getBpjsJkmRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        item.setBpjsKetenagakerjaanEmployee(jhtEmployee.add(jpEmployee));
        item.setBpjsKetenagakerjaanCompany(jhtCompany.add(jpCompany).add(jkkCompany).add(jkmCompany));

        // 3. PPh 21 Calculation (Simplified Progressive Tax)
        // For simplicity in this demo, we apply the bracket that the annualized gross pay falls into.
        BigDecimal annualizedGross = grossPay.multiply(BigDecimal.valueOf(12));
        
        List<TaxBracket> brackets = settings.getTaxBrackets();
                
        BigDecimal taxRate = BigDecimal.ZERO;
        for (TaxBracket bracket : brackets) {
            if (annualizedGross.compareTo(bracket.getMinAmount()) >= 0 && 
               (bracket.getMaxAmount() == null || annualizedGross.compareTo(bracket.getMaxAmount()) <= 0)) {
                taxRate = bracket.getRate();
                break;
            }
        }
        
        item.setPph21Tax(grossPay.multiply(taxRate)); // rate is already stored as decimal

        // 4. Finalize Totals
        item.calculateTotals();
    }
}
