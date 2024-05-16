package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.Administrator;

public interface AdminRepository extends CrudRepository<Administrator, Long> {
}