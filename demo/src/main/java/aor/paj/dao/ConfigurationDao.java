package aor.paj.dao;

import aor.paj.entity.ConfigurationEntity;
import aor.paj.entity.NotificationEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.List;

@Stateless
public class ConfigurationDao extends AbstractDao<ConfigurationEntity> {

    private static final long serialVersionUID = 1L;

    public ConfigurationDao() {
        super(ConfigurationEntity.class);
    }

    public ConfigurationEntity findById(int id) {
        try {
            return em.createNamedQuery("Configuration.findById", ConfigurationEntity.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}

