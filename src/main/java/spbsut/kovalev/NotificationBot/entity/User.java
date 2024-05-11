package spbsut.kovalev.NotificationBot.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalTime;

@Getter
@Setter
@Entity(name="users")
public class User extends TelegramUser{

    private int groupId;
    private LocalTime startQuietTime;
    private LocalTime endQuietTime;
    private Timestamp registeredAt;
}
