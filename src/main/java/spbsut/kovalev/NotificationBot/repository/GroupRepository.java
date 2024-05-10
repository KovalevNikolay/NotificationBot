package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.Group;

public interface GroupRepository extends CrudRepository<Group, Integer> {
}
