package spbsut.kovalev.NotificationBot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name="template")
public class MessageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer templateId;
    @Column(unique = true)
    private String messageText;

    @Override
    public String toString() {
        return STR."Template{templateId=\{templateId}, messageText='\{messageText}\{'\''}\{'}'}";
    }
}
