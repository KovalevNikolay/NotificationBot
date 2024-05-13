package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.Group;

import java.util.Optional;

public interface GroupRepository extends CrudRepository<Group, Integer> {
    @Query("SELECT g FROM groupTable g WHERE g.groupName = ?1")
    Optional<Group> findByGroupName(final String groupName);
}
