package aor.paj.validator;

import aor.paj.dao.CategoryDao;
import aor.paj.dto.TaskDto;
import aor.paj.utils.Priority;
import aor.paj.utils.State;
import jakarta.ejb.EJB;

import java.time.LocalDate;

public class TaskValidator {

    //function that verifys if atributes of a new task are not null or empty, then verifys if initial date is before final date
    public static boolean isValidTask(TaskDto t) {
        if (t != null) {
            if (t.getTitle() != null && !t.getTitle().isEmpty()) {
                if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                    if (t.getPriority() != null && t.getPriority() != 0) {
                        if (isValidDates(t)) {
                            if (t.getCategory() != null && !t.getCategory().isEmpty()) {
                                if (isValidCategory(t.getCategory())) {
                                    return true;
                                }
                            }

                        }
                    }
                }
            }
        }
        return false;
    }


    //function that verifys the task to edit
    public static boolean isValidTaskEdit(TaskDto t) {
        if (t != null) {
            if (t.getTitle() != null && !t.getTitle().isEmpty()) {
                if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                    if (t.getPriority() != null && t.getPriority() != 0) {
                        if (isValidDates(t)) {
                            if (t.getStatus() != 0) {
                                if (t.getStatus() != null) {
                                    if (isValidStatus(t.getStatus())) {
                                        if (t.getCategory() != null && !t.getCategory().isEmpty()) {
                                            if (isValidCategory(t.getCategory())) {
                                                return true;
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isValidDates(TaskDto t) {
        LocalDate initialDate = t.getInitialDate();
        LocalDate finalDate = t.getFinalDate();

        if (initialDate != null && finalDate != null) {
            // Both dates are present, check if InitialDate is before or same as FinalDate
            return !initialDate.isAfter(finalDate);
        } else if (initialDate != null && finalDate == null) {
            // Only InitialDate is present, no restriction on FinalDate
            return true;
        } else if (initialDate == null) {
            // InitialDate is null, invalid case
            return false;
        } else {
            // Both InitialDate and FinalDate are null, consider as valid (optional based on requirements)
            return true;
        }
    }

    public static boolean isValidStatus(int status) {
        return status == State.TODO.getValue() || status == State.DOING.getValue() || status == State.DONE.getValue();
    }

    public static boolean isValidCategory(String category) {
        CategoryDao categoryDao = new CategoryDao();
        return categoryDao.findCategoryByTitle(category) == null;
    }
}
