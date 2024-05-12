package spbsut.kovalev.NotificationBot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
@Entity(name="message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;
    private String messageText;
    private Timestamp timeSending;
    private Long senderId;

    @Override
    public String toString() {
        return STR."Message{messageId=\{messageId}, messageText='\{messageText}\{'\''}, timeSending=\{timeSending}, senderId=\{senderId}\{'}'}";
    }
}
