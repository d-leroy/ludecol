package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.ExpertGame;
import com.irisa.ludecol.domain.subdomain.GameMode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface ExpertGameRepository extends MongoRepository<ExpertGame, String> {

    List<ExpertGame> findAllByImg(String img);

    List<ExpertGame> findAllByUsr(String usr);

    List<ExpertGame> findAllByUsrAndCompleted(String usr, boolean completed);

    List<ExpertGame> findAllByUsrAndGameMode(String usr, GameMode gameMode);

    List<ExpertGame> findAllByUsrAndGameModeAndCompleted(String usr, GameMode gameMode, boolean completed);

    List<ExpertGame> findAllByImgAndUsrAndGameMode(String img, String usr, GameMode gameMode);

}
