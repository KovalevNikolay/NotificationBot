package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.MessageTemplate;

import java.util.Optional;

public interface TemplateRepository extends CrudRepository<MessageTemplate, Integer> {
    @Query("SELECT t FROM template t WHERE t.messageText = ?1")
    Optional<MessageTemplate> findByMessageText(final String text);
}