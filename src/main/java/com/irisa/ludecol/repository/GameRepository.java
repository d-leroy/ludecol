package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.subdomain.GameMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface GameRepository extends MongoRepository<Game, String> {

    List<Game> findAllByUsr(String usr);

    List<Game> findAllByImg(String img);

    List<Game> findAllByGameMode(GameMode gameMode);

    List<Game> findAllByImgAndGameModeAndCompleted(String img, GameMode gameMode, boolean completed);

    List<Game> findAllByUsrAndGameModeAndCompleted(String login, GameMode gameMode, boolean completed);

    List<Game> findAllByUsrAndCompleted(String login, boolean completed);

    List<Game> findAllByUsrAndGameMode(String login, GameMode gameMode);

    List<Game> findFirst5ByUsrAndCompletedAndScoreGreaterThan(String login, boolean completed, int score, Sort sort);

    Page<Game> findByUsr(String usr, Pageable pageable);

    Page<Game> findByImgAndGameModeAndCompleted(String img, GameMode gameMode, boolean completed, Pageable pageable);

    Page<Game> findByUsrAndGameModeAndCompleted(String login, GameMode gameMode, boolean completed, Pageable pageable);

    Page<Game> findByUsrAndCompleted(String login, boolean completed, Pageable pageable);

    Page<Game> findByUsrAndGameMode(String login, GameMode gameMode, Pageable pageable);

    Page<Game> findByUsrAndScoreGreaterThan(String usr, int score, Pageable pageable);

    Page<Game> findByImgAndGameModeAndCompletedAndScoreGreaterThan(String img, GameMode gameMode, boolean completed, int score, Pageable pageable);

    Page<Game> findByUsrAndGameModeAndCompletedAndScoreGreaterThan(String login, GameMode gameMode, boolean completed, int score, Pageable pageable);

    Page<Game> findByUsrAndCompletedAndScoreGreaterThan(String login, boolean completed, int score, Pageable pageable);

    Page<Game> findByUsrAndGameModeAndScoreGreaterThan(String login, GameMode gameMode, int score, Pageable pageable);
}
