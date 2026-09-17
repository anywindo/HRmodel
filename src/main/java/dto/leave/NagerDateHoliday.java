package dto.leave;

public class NagerDateHoliday {
    private String date;
    private String name;
    private String localName;
    private String countryCode;
    private boolean fixed;
    private boolean global;

    public NagerDateHoliday() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocalName() { return localName; }
    public void setLocalName(String localName) { this.localName = localName; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public boolean isFixed() { return fixed; }
    public void setFixed(boolean fixed) { this.fixed = fixed; }
    public boolean isGlobal() { return global; }
    public void setGlobal(boolean global) { this.global = global; }
}
