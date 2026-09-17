package service.leave;

import com.hr.dto.leave.LeaveSettingsDTO;
import com.hr.dto.leave.PublicHolidayDTO;
import model.leave.LeaveSettings;
import model.leave.PublicHoliday;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.leave.LeaveSettingsRepository;
import repository.leave.PublicHolidayRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;
import dto.leave.NagerDateHoliday;

@Service
@Transactional
public class LeaveSettingsService {

    private final LeaveSettingsRepository leaveSettingsRepository;
    private final PublicHolidayRepository publicHolidayRepository;

    public LeaveSettingsService(LeaveSettingsRepository leaveSettingsRepository, PublicHolidayRepository publicHolidayRepository) {
        this.leaveSettingsRepository = leaveSettingsRepository;
        this.publicHolidayRepository = publicHolidayRepository;
    }

    public LeaveSettingsDTO getSettings() {
        LeaveSettings settings = leaveSettingsRepository.findAll().stream().findFirst().orElseGet(() -> {
            LeaveSettings defaultSettings = new LeaveSettings();
            return leaveSettingsRepository.save(defaultSettings);
        });
        
        return mapToDTO(settings);
    }

    public LeaveSettingsDTO updateSettings(LeaveSettingsDTO dto) {
        LeaveSettings settings = leaveSettingsRepository.findAll().stream().findFirst().orElse(new LeaveSettings());
        
        settings.setEnableAccrual(dto.isEnableAccrual());
        settings.setAccrualRatePerMonth(dto.getAccrualRatePerMonth());
        settings.setMaxCarryOverDays(dto.getMaxCarryOverDays());
        settings.setDefaultAnnualLeaveDays(dto.getDefaultAnnualLeaveDays());
        settings.setDefaultSickLeaveDays(dto.getDefaultSickLeaveDays());
        
        return mapToDTO(leaveSettingsRepository.save(settings));
    }

    public List<PublicHolidayDTO> getAllHolidays() {
        return publicHolidayRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PublicHolidayDTO addHoliday(PublicHolidayDTO dto) {
        PublicHoliday holiday = new PublicHoliday(dto.getDate(), dto.getDescription());
        return mapToDTO(publicHolidayRepository.save(holiday));
    }

    public void deleteHoliday(Long id) {
        publicHolidayRepository.deleteById(id);
    }

    public int importHolidays(int year, String countryCode) {
        // Delete existing holidays for that year
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        publicHolidayRepository.deleteByDateBetween(startDate, endDate);

        // Fetch from API
        String url = String.format("https://date.nager.at/api/v3/PublicHolidays/%d/%s", year, countryCode);
        RestTemplate restTemplate = new RestTemplate();
        NagerDateHoliday[] apiResponse = restTemplate.getForObject(url, NagerDateHoliday[].class);

        if (apiResponse == null || apiResponse.length == 0) {
            return 0;
        }

        List<PublicHoliday> newHolidays = Arrays.stream(apiResponse).map(dto -> {
            PublicHoliday ph = new PublicHoliday();
            ph.setDate(LocalDate.parse(dto.getDate()));
            ph.setName(dto.getName());
            return ph;
        }).collect(Collectors.toList());

        publicHolidayRepository.saveAll(newHolidays);
        return newHolidays.size();
    }

    private LeaveSettingsDTO mapToDTO(LeaveSettings settings) {
        LeaveSettingsDTO dto = new LeaveSettingsDTO();
        dto.setId(settings.getId());
        dto.setEnableAccrual(settings.isEnableAccrual());
        dto.setAccrualRatePerMonth(settings.getAccrualRatePerMonth());
        dto.setMaxCarryOverDays(settings.getMaxCarryOverDays());
        dto.setDefaultAnnualLeaveDays(settings.getDefaultAnnualLeaveDays());
        dto.setDefaultSickLeaveDays(settings.getDefaultSickLeaveDays());
        return dto;
    }

    private PublicHolidayDTO mapToDTO(PublicHoliday holiday) {
        return new PublicHolidayDTO(holiday.getId(), holiday.getDate(), holiday.getName());
    }
}
