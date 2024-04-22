package aor.paj.dto;

import java.time.LocalDate;

public class DashboardTaskLineChartDto {

    private LocalDate initialDate;
    private LocalDate finalDate;

    public DashboardTaskLineChartDto(LocalDate finalDate, LocalDate initialDate) {

        this.initialDate = initialDate;
        this.finalDate = finalDate;
    }

    // Getters and setters
    public LocalDate getInitialDate() {
        return initialDate;
    }

    public void setInitalDate(LocalDate initalDate) {
        this.initialDate = initalDate;
    }

    public LocalDate getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(LocalDate finalDate) {
        this.finalDate = finalDate;
    }
}
