package aor.paj.bean;

import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.TokenDto;
import aor.paj.entity.TokenEntity;
import aor.paj.entity.UserEntity;
import aor.paj.mapper.TokenMapper;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TokenBean {

    @EJB
    TokenDao tokenDao;

    @EJB
    UserDao userDao;

    public String generateNewToken() {
        return UUID.randomUUID().toString();
    }

    public TokenDto generateToken(UserEntity userEntity) {
        String tokenValue = generateNewToken();
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(1); // Adjust as per your requirements
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setTokenValue(tokenValue);
        tokenEntity.setExpirationTime(expirationTime);
        tokenEntity.setUser(userEntity);
        tokenDao.persist(tokenEntity);
        return TokenMapper.convertTokenEntityToTokenDto(tokenEntity);
    }

    public boolean isValidToken(String tokenValue) {
        TokenEntity tokenEntity = tokenDao.findTokenByValue(tokenValue);
        return tokenEntity != null && !tokenEntity.getExpirationTime().isBefore(LocalDateTime.now());
    }

    public boolean isValidTokenForUser(String tokenValue, String username) {
        TokenEntity tokenEntity = tokenDao.findTokenByValue(tokenValue);
        if (tokenEntity != null) {
            // Retrieve the user entity based on the username
            UserEntity userEntity = userDao.findUserByUsername(username);
            if (userEntity != null) {
                // Check if the token is associated with the retrieved user entity
                return tokenEntity.getUser().getId() == userEntity.getId() && !tokenEntity.getExpirationTime().isBefore(LocalDateTime.now());
            }
        }
        return false;
    }


    public void deleteToken(String tokenValue) {
        TokenEntity tokenEntity = tokenDao.findTokenByValue(tokenValue);
        System.out.println(tokenEntity);
        System.out.println(tokenValue);
        if (tokenEntity != null) {
            tokenDao.remove(tokenEntity);
        }
    }

    public boolean deleteTokensForUser(UserEntity userEntity) {
        List<TokenEntity> tokens = tokenDao.findTokensByUserId(userEntity.getId());
        if (!tokens.isEmpty()) {
            for (TokenEntity token : tokens) {
                tokenDao.remove(token);
            }
            return true;
        }
        return false;
    }
}
