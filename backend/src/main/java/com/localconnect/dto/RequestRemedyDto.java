package com.localconnect.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;

public class RequestRemedyDto {

    @NotBlank(message = "Action is required (EXTEND_WINDOW, EXPAND_RADIUS, MODIFY_DETAILS, CANCEL_REQUEST)")
    private String action;

    private Integer additionalHours;
    private Integer additionalRadiusKm;
    private Integer modifiedQuantity;
    private LocalDate modifiedDate;
    private LocalTime modifiedTime;
    private String modifiedInstructions;

    public RequestRemedyDto() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getAdditionalHours() {
        return additionalHours;
    }

    public void setAdditionalHours(Integer additionalHours) {
        this.additionalHours = additionalHours;
    }

    public Integer getAdditionalRadiusKm() {
        return additionalRadiusKm;
    }

    public void setAdditionalRadiusKm(Integer additionalRadiusKm) {
        this.additionalRadiusKm = additionalRadiusKm;
    }

    public Integer getModifiedQuantity() {
        return modifiedQuantity;
    }

    public void setModifiedQuantity(Integer modifiedQuantity) {
        this.modifiedQuantity = modifiedQuantity;
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDate modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public LocalTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getModifiedInstructions() {
        return modifiedInstructions;
    }

    public void setModifiedInstructions(String modifiedInstructions) {
        this.modifiedInstructions = modifiedInstructions;
    }
}
