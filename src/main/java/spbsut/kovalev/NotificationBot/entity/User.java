package spbsut.kovalev.NotificationBot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity(name="users")
public class User {

    @Id
    private long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    private String bio;
    private Timestamp registeredAt;

    @Override
    public String toString() {
        return STR."User{chatId=\{chatId}, firstName='\{firstName}\{'\''}, lastName='\{lastName}\{'\''}, userName='\{userName}\{'\''}, bio='\{bio}\{'\''}, registeredAt=\{registeredAt}\{'}'}";
    }
}
