package com.example.lifevault.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MpinSetupRequest {
    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "MPIN is required")
    @Size(min = 4, max = 6, message = "MPIN must be between 4 and 6 digits")
    @Pattern(regexp = "^[0-9]*$", message = "MPIN must contain only numbers")
    private String mpin;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }
}
