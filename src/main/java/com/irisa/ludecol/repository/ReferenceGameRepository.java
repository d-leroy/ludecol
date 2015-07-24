package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.ReferenceGame;
import com.irisa.ludecol.domain.subdomain.GameMode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface ReferenceGameRepository extends MongoRepository<ReferenceGame, String> {

    ReferenceGame findByImgAndGameMode(String img, GameMode gameMode);

    List<ReferenceGame> findAllByImg(String img);


}
