package aor.paj.mapper;

import aor.paj.dto.TokenDto;
import aor.paj.entity.TokenEntity;

public class TokenMapper {
    public TokenMapper() {
    }

    public static TokenEntity convertTokenDtoToTokenEntity(TokenDto tokenDto) {
        TokenEntity tokenEntity = new TokenEntity();

        tokenEntity.setExpirationTime(tokenDto.getExpirationTime());
        tokenEntity.setTokenValue(tokenDto.getTokenValue());
        return tokenEntity;
    }

    public static TokenDto convertTokenEntityToTokenDto(TokenEntity tokenEntity) {
        TokenDto tokenDto = new TokenDto();

        tokenDto.setExpirationTime(tokenEntity.getExpirationTime());
        tokenDto.setTokenValue(tokenEntity.getTokenValue());

        return tokenDto;
    }
}
