package aor.paj.dto;

import aor.paj.entity.UserEntity;

import java.time.LocalDateTime;

public class DashboardLineChartDto {

    private LocalDateTime registTime;

    public DashboardLineChartDto(LocalDateTime registTime) {

        this.registTime = registTime;
    }

    // Getters and setters

    public LocalDateTime getRegistTime() {
        return registTime;
    }

    public void setRegistTime(LocalDateTime registTime) {
        this.registTime = registTime;
    }


}
