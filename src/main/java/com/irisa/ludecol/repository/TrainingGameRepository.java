package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.TrainingGame;
import com.irisa.ludecol.domain.subdomain.GameMode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface TrainingGameRepository extends MongoRepository<TrainingGame, String> {

    List<TrainingGame> findAllByUsr(String usr);

    List<TrainingGame> findAllByUsrAndCompleted(String usr, boolean completed);

    List<TrainingGame> findAllByUsrAndGameMode(String usr, GameMode gameMode);

    List<TrainingGame> findAllByUsrAndGameModeAndCompleted(String usr, GameMode gameMode, boolean completed);

}
