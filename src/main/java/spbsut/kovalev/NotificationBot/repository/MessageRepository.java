package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.Message;

public interface MessageRepository extends CrudRepository<Message, Integer> {
}