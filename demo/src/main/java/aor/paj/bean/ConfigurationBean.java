package aor.paj.bean;

import aor.paj.dao.ConfigurationDao;
import aor.paj.dao.NotificationDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.*;
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

    public void updateTokenExpirationTime(int newTokenExpirationTime) {
        ConfigurationEntity configurationEntity = configurationDao.findById(1);
        System.out.println(configurationEntity);
        if (configurationEntity != null) {
            configurationEntity.setTokenExpirationTime(newTokenExpirationTime);
            System.out.println(configurationEntity);
            configurationDao.merge(configurationEntity);
        }
    }

    public ConfigurationDto getTokenExpirationTime(){
        ConfigurationEntity configurationEntity = configurationDao.findById(1);
        ConfigurationDto tokenExpirationTimeDto = new ConfigurationDto();
        if (configurationEntity != null) {

            tokenExpirationTimeDto.setId(configurationEntity.getId());
            tokenExpirationTimeDto.setTokenExpirationTime((configurationEntity.getTokenExpirationTime()));
        }
        return tokenExpirationTimeDto;
    }
}

