package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.User;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    @Query("SELECT u FROM users u WHERE u.groupId = ?1")
    List<User> findByGroupId(int groupId);
}