package spbsut.kovalev.NotificationBot.entity;

import jakarta.persistence.Entity;

@Entity(name = "admins")
public class Administrator extends TelegramUser {
    @Override
    public String toString() {
        return STR."Administrator{chatId=\{chatId}, firstName=\{firstName}, lastName=\{lastName}, userName=\{userName}, bio=\{bio}\{'}'}";
    }
}