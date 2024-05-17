package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.Scheduler;

public interface SchedulerRepository extends CrudRepository<Scheduler, Long> {
}
