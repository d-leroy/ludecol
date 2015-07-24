package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.ProcessedGame;
import com.irisa.ludecol.domain.subdomain.GameMode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface ProcessedGameRepository extends MongoRepository<ProcessedGame, String> {

    ProcessedGame findByImgAndGameMode(String img, GameMode gameMode);

    List<ProcessedGame> findAllByImg(String img);


}
