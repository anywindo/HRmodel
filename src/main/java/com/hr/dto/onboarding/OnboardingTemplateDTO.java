package com.hr.dto.onboarding;

import java.util.List;

public class OnboardingTemplateDTO {
    private Long id;
    private String name;
    private String type;
    private String description;
    private List<TemplateTaskDTO> tasks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<TemplateTaskDTO> getTasks() { return tasks; }
    public void setTasks(List<TemplateTaskDTO> tasks) { this.tasks = tasks; }
}
