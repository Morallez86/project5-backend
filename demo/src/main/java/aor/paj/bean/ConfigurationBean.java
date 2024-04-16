package aor.paj.bean;

import aor.paj.dao.ConfigurationDao;
import aor.paj.dao.NotificationDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.CategoryDto;
import aor.paj.dto.MessageDto;
import aor.paj.dto.NotificationDto;
import aor.paj.dto.UserDto;
import aor.paj.entity.ConfigurationEntity;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.NotificationEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.MessageMapper;
import aor.paj.mapper.NotificationMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.NotificationOptions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ConfigurationBean {

    @EJB
    ConfigurationDao configurationDao;

    public int generateIdDataBase() {
        int id = 1;
        boolean idAlreadyExists;

        do {
            idAlreadyExists = false;
            ConfigurationEntity configurationEntity = configurationDao.findById(id);
            if (configurationEntity != null) {
                id++;
                idAlreadyExists = true;
            }
        } while (idAlreadyExists);
        return id;
    }

    public void createDefaultConfigurationIfNotExistent() {
        if (configurationDao.findById(1) == null) {
            ConfigurationEntity configurationEntity = new ConfigurationEntity();
            configurationEntity.setTokenExpirationTime(1);
            configurationEntity.setId(generateIdDataBase());
            configurationDao.persist(configurationEntity);
        }

    }
}

