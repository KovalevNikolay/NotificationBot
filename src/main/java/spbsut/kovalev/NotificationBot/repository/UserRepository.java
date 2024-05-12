package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {
    @Query("SELECT u FROM users u WHERE u.groupId = ?1")
    Iterable<User> findByGroupId(final Integer groupId);
}