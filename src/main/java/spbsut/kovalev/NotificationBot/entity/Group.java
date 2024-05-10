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
@Entity(name = "user_group")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int groupId;

    @Column(name = "group_name", unique = true)
    private String groupName;
    private int countUsers;

    @Override
    public String toString() {
        return STR."Group{groupName='\{groupName}\{'\''}, countUsers=\{countUsers}\{'}'}";
    }
}
