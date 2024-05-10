package spbsut.kovalev.NotificationBot.interfaces;

import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {
}
