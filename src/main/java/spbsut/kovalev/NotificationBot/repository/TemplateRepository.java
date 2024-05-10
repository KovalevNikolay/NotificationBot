package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.MessageTemplate;

public interface TemplateRepository extends CrudRepository<MessageTemplate, Integer> {
}
