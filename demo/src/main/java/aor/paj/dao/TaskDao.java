package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Stateless
public class TaskDao extends AbstractDao<TaskEntity>{

    private static final long serialVersionUID = 1L;

    public TaskDao() {
        super(TaskEntity.class);
    }

    public TaskDao(Class<TaskEntity> clazz) {
        super(clazz);
    }

    public TaskEntity findTaskById(int id) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public TaskEntity findTaskByTitle(String title) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskByTitle").setParameter("title", title)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TaskEntity> findTaskByOwnerId(int id) {
        try {
            return em.createNamedQuery("Task.findTaskByOwnerId").setParameter("id", id).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    //Function that returns all tasks that have active == true
    public List<TaskEntity> getActiveTasks() {
        try {
            return em.createNamedQuery("Task.getActiveTasks").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TaskEntity> findTasksByCategory(CategoryEntity category){
        try {
            return em.createNamedQuery("Task.findTaskByCategory").setParameter("category", category).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TaskEntity> getTasksByCategoryAndOwner(UserEntity owner, CategoryEntity category){
        try {
            return em.createNamedQuery("Task.findTaskByCategoryAndOwner").setParameter("category", category).setParameter("owner", owner).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    //Function that returns all the tasks of database mysql
    public List<TaskEntity> getAllTasks() {
        return em.createNamedQuery("Task.getAllTasks").getResultList();
    }

    // Function that returns all inactive tasks
    public List<TaskEntity> getInactiveTasks() {
        try {
            return em.createNamedQuery("Task.getInactiveTasks").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TaskEntity> getActiveTasksOrderedByPriorityDesc() {
        try {
            return em.createNamedQuery("Task.getActiveTasksOrderedByPriority", TaskEntity.class).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Object[]> countTasksByStatus(int userId) {
        try {
            return em.createNamedQuery("Task.countTasksByStatus", Object[].class)
                    .setParameter("userId", userId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    // Method to count total tasks for a specific user, this must be of Long type because of COUNT
    public long countTotalTasksByUser(int userId) {
        try {
            return (long) em.createNamedQuery("Task.countTotalTasksByUser")
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    public long countAllTasks() {
        try {
            return (long) em.createNamedQuery("Task.countAllTasks")
                    .getSingleResult();
        } catch (NoResultException e) {
            return 0; // Return 0 if no tasks found
        }
    }

    public List<Object[]> countTotalTasksByStatus() {
        try {
            return em.createNamedQuery("Task.countTotalTasksByStatus", Object[].class)
                    .getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList(); // Return empty list if no tasks found
        }
    }

    public List<Object[]> countTasksByCategory() {
        try {
            return em.createNamedQuery("Task.countTasksByCategory", Object[].class)
                    .getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList(); // Return empty list if no tasks found
        }
    }

    public List<TaskEntity> findTasksByStatus(int status) {
        try {
            return em.createNamedQuery("Task.findTasksByStatus", TaskEntity.class)
                    .setParameter("statusValue", status)
                    .getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
}
