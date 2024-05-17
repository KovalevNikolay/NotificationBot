package spbsut.kovalev.NotificationBot.repository;

import org.springframework.data.repository.CrudRepository;
import spbsut.kovalev.NotificationBot.entity.SilenceMode;

public interface SilenceModeRepository extends CrudRepository<SilenceMode, Integer> {
}
