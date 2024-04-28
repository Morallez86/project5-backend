package aor.paj.bean;
import aor.paj.websocket.Notifier;
import aor.paj.websocket.Chat;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import java.time.LocalDateTime;


@Singleton
public class TimerBean {
    @Inject
    Notifier notifier;

    @Inject
    Chat chat;

    @Inject
    TokenBean tokenBean;

    @Inject
    UserBean userBean;


    @Schedule(second="*/60", minute="*", hour="*")
    public void automaticTimer(){
        String msg = "Token removed!";
        tokenBean.removeExpiredTokens();
        userBean.removeUnvalidatedUsers();
        userBean.removeEmailValidationPasswordRetrieval();
    }
}
