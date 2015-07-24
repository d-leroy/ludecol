package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.GameNotification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the Image entity.
 */
public interface GameNotificationRepository extends MongoRepository<GameNotification,String> {

    List<GameNotification> findAllByUsrAndViewed(String usr, boolean viewed);

    List<GameNotification> findAllByUsrAndSent(String usr, boolean sent);
}
