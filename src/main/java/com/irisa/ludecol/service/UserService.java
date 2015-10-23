package com.irisa.ludecol.service;

import com.codahale.metrics.annotation.Timed;
import com.irisa.ludecol.domain.Authority;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.Objective;
import com.irisa.ludecol.domain.User;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.Pair;
import com.irisa.ludecol.repository.*;
import com.irisa.ludecol.security.SecurityUtils;
import com.irisa.ludecol.service.util.RandomUtil;
import com.irisa.ludecol.web.rest.dto.UserStatisticsDTO;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ObjectiveRepository objectiveRepository;

    @Inject
    private GameRepository gameRepository;

    @Inject
    private PersistentTokenRepository persistentTokenRepository;

    @Inject
    private AuthorityRepository authorityRepository;

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                userRepository.save(user);
                log.debug("Activated user: {}", user);
                return user;
            });
        return Optional.empty();
    }

    public User createUserInformation(String login, String password, String firstName, String lastName, String email,
                                      String langKey) {
        User newUser = new User();
        Authority authority = authorityRepository.findOne("ROLE_USER");
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setLangKey(langKey);
        // new user is not active
        newUser.setActivated(true);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public void updateUserInformation(String firstName, String lastName, String email) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentLogin()).ifPresent(u -> {
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setEmail(email);
            userRepository.save(u);
            log.debug("Changed Information for User: {}", u);
        });
    }

    public void changePassword(String password) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentLogin()).ifPresent(u -> {
            String encryptedPassword = passwordEncoder.encode(password);
            u.setPassword(encryptedPassword);
            userRepository.save(u);
            log.debug("Changed password for User: {}", u);
        });
    }

    public User getUserWithAuthorities() {
        User currentUser = userRepository.findOneByLogin(SecurityUtils.getCurrentLogin()).get();
        currentUser.getAuthorities().size(); // eagerly load the association
        return currentUser;
    }

    public UserStatisticsDTO getUserStatistics(String login) {

        UserStatisticsDTO result = new UserStatisticsDTO();
        List<UserStatisticsDTO.GameModeStatistics> gameModeStatistics = new ArrayList<>();
        int totalEarnedPoints = 0;

        List<Game> allStarsGames = gameRepository.findAllByUsrAndGameModeAndCompleted(login, GameMode.AllStars, true);
        int averageScore = 0;
        int nbGames = 0;
        for (Game allStarsGame : allStarsGames) {
            if(allStarsGame.getScore() >= 0) {
                averageScore += allStarsGame.getScore();
                totalEarnedPoints += allStarsGame.getScore() - 50;
                nbGames++;
            }
        }
        averageScore = nbGames > 0 ? averageScore / nbGames : 0;
        UserStatisticsDTO.GameModeStatistics allStars = new UserStatisticsDTO.GameModeStatistics(GameMode.AllStars,averageScore,nbGames);
        gameModeStatistics.add(allStars);

        List<Game> plantIdentificationGames = gameRepository.findAllByUsrAndGameModeAndCompleted(login, GameMode.PlantIdentification, true);
        averageScore = 0;
        nbGames = 0;
        for (Game plantIdentificationGame : plantIdentificationGames) {
            if(plantIdentificationGame.getScore() >= 0) {
                averageScore += plantIdentificationGame.getScore();
                totalEarnedPoints += plantIdentificationGame.getScore() - 50;
                nbGames++;
            }
        }
        averageScore = nbGames > 0 ? averageScore / nbGames : 0;
        UserStatisticsDTO.GameModeStatistics plantIdentification = new UserStatisticsDTO.GameModeStatistics(GameMode.AnimalIdentification,averageScore,nbGames);
        gameModeStatistics.add(plantIdentification);

        List<Game> animalIdentificationGames = gameRepository.findAllByUsrAndGameModeAndCompleted(login, GameMode.AnimalIdentification, true);
        averageScore = 0;
        nbGames = 0;
        for (Game animalIdentificationGame : animalIdentificationGames) {
            if(animalIdentificationGame.getScore() >= 0) {
                averageScore += animalIdentificationGame.getScore();
                totalEarnedPoints += animalIdentificationGame.getScore() - 50;
                nbGames++;
            }
        }
        averageScore = nbGames > 0 ? averageScore / nbGames : 0;
        UserStatisticsDTO.GameModeStatistics animalIdentification = new UserStatisticsDTO.GameModeStatistics(GameMode.AnimalIdentification,averageScore,nbGames);
        gameModeStatistics.add(animalIdentification);

        result.setGameModeStatistics(gameModeStatistics);
        result.setTotalEarnedPoints(totalEarnedPoints);

        User player = userRepository.findOneByLogin(login).get();
        result.setBonusPoints(player.getBonusPoints());
        result.setMinRank(player.getBestRank());
        result.setMeanRank(player.getMeanRank());

        return result;
    }

    public void updateSkippedList(String usr, String img, GameMode mode) {
        User user = userRepository.findOneByLogin(usr).get();
        Set<Pair<String,GameMode>> skippedImages = user.getSkippedImages();
        Pair<String,GameMode> pair = new Pair(img,mode);
        skippedImages.add(pair);
        userRepository.save(user);
    }

    /**
     * Adds a new objective to users that have less than 3 ongoing objectives.
     * This is scheduled to get fired every 10 minutes.
     */
//    @Scheduled(cron = "5/30 * * * * ?")
//    @Timed
//    public void addObjective() {
//        List<User> users = userRepository.findAll();
//        Random rand = new Random();
//        users.stream()
//            .filter(user -> user.getActivated())
//            .forEach(user -> {
//                List<Objective> objectives = objectiveRepository.findAllByUsr(user.getLogin());
//                List<Objective> ongoingObjectives = new ArrayList<>();
//                objectives.stream().filter(o -> o.getNbCompletedGames() < o.getNbGamesToComplete()).forEach(ongoingObjectives::add);
//                if (ongoingObjectives.size() < 3) {
//                    EnumSet<GameMode> pool = EnumSet.of(GameMode.AllStars,GameMode.AnimalIdentification,GameMode.PlantIdentification);
//                    ongoingObjectives.forEach(o -> pool.remove(o.getGameMode()));
//                    Objective objective = new Objective();
//                    objective.setUsr(user.getLogin());
//                    GameMode gameMode = (GameMode) pool.toArray()[rand.nextInt(pool.size())];
//                    switch (gameMode) {
//                        //TODO refine number of games (1 and 2 are totally arbitrary numbers)
//                        case AllStars: objective.setNbGamesToComplete(2); break;
//                        case AnimalIdentification:
//                        case PlantIdentification: objective.setNbGamesToComplete(1); break;
//                        default: objective.setNbGamesToComplete(0); break;
//                    }
//                    objective.setGameMode(gameMode);
//                    objective.setCreationDate(new DateTime());
//                    log.debug("Adding objective : {}", objective);
//                    objectiveRepository.save(objective);
//                }
//            });
//    }

    /**
     * Persistent Token are used for providing automatic authentication, they should be automatically deleted after
     * 30 days.
     * <p/>
     * <p>
     * This is scheduled to get fired everyday, at midnight.
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeOldPersistentTokens() {
        LocalDate now = new LocalDate();
        persistentTokenRepository.findByTokenDateBefore(now.minusMonths(1)).stream().forEach(token ->{
            log.debug("Deleting token {}", token.getSeries());
            persistentTokenRepository.delete(token);
        });
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p/>
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        DateTime now = new DateTime();
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(3));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
        }
    }
}
