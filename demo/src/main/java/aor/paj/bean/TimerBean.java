package aor.paj.bean;
import aor.paj.websocket.Notifier;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;


@Singleton
public class TimerBean {
    @Inject
    Notifier notifier;

    @Inject
    TokenBean tokenBean;

    @Schedule(second="*/60", minute="*", hour="*")
    public void automaticTimer(){
        String msg = "This is just a reminder!";
        System.out.println(msg);
        notifier.send("mytoken", msg);

        // Call removeExpiredTokens method from TokenBean
        tokenBean.removeExpiredTokens();
    }
}
