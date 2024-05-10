package spbsut.kovalev.NotificationBot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
@Entity(name="admin")
public class Administrator {

    @Id
    private long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    private Timestamp registeredAt;

    @Override
    public String toString() {
        return STR."Admin{chatId=\{chatId}, firstName='\{firstName}\{'\''}, lastName='\{lastName}\{'\''}, userName='\{userName}\{'\''}, registeredAt=\{registeredAt}\{'}'}";
    }
}
