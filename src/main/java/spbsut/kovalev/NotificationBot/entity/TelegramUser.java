package spbsut.kovalev.NotificationBot.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class TelegramUser {
    @Id
    protected Long chatId;
    protected String firstName;
    protected String lastName;
    protected String userName;
    protected String bio;
}
