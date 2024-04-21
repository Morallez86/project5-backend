package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CategoryTaskCountDto {

    private String categoryTitle;
    private Long taskCount;

    // Constructors
    public CategoryTaskCountDto() {
    }

    public CategoryTaskCountDto(String categoryTitle, Long taskCount) {
        this.categoryTitle = categoryTitle;
        this.taskCount = taskCount;
    }

    // Getters and Setters
    @XmlElement
    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    @XmlElement
    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long taskCount) {
        this.taskCount = taskCount;
    }

    @Override
    public String toString() {
        return "CategoryTaskCountDto{" +
                "categoryTitle='" + categoryTitle + '\'' +
                ", taskCount=" + taskCount +
                '}';
    }
}
