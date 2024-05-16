package spbsut.kovalev.NotificationBot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "groupTable")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer groupId;

    @Column(unique = true)
    private String groupName;
    private Integer countUsers;

    @Override
    public String toString() {
        return STR."Group{groupName='\{groupName}\{'\''}, countUsers=\{countUsers}\{'}'}";
    }
}