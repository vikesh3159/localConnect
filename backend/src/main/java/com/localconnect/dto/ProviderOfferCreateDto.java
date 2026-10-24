package com.localconnect.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProviderOfferCreateDto {

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @Min(value = 1, message = "Offered quantity must be at least 1")
    private int offeredQuantity;

    @NotNull(message = "Quoted amount is required")
    @Min(value = 1, message = "Quoted amount must be positive")
    private BigDecimal quotedAmount;

    @AssertTrue(message = "All vehicle bookings must confirm certified driver is included")
    private boolean includesDriver = true;

    public ProviderOfferCreateDto() {
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public int getOfferedQuantity() {
        return offeredQuantity;
    }

    public void setOfferedQuantity(int offeredQuantity) {
        this.offeredQuantity = offeredQuantity;
    }

    public BigDecimal getQuotedAmount() {
        return quotedAmount;
    }

    public void setQuotedAmount(BigDecimal quotedAmount) {
        this.quotedAmount = quotedAmount;
    }

    public boolean isIncludesDriver() {
        return includesDriver;
    }

    public void setIncludesDriver(boolean includesDriver) {
        this.includesDriver = includesDriver;
    }
}
