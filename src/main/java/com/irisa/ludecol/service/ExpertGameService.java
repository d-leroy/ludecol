package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.*;
import com.irisa.ludecol.domain.subdomain.*;
import com.irisa.ludecol.repository.*;
import com.irisa.ludecol.web.rest.GameNotificationResource;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by dorian on 07/05/15.
 */
@Service
public class ExpertGameService {

    private final Logger log = LoggerFactory.getLogger(ExpertGameService.class);

    @Inject
    private ReferenceGameRepository referenceGameRepository;

    @Inject
    private ExpertGameRepository expertGameRepository;

    @Inject
    private ProcessedGameRepository processedGameRepository;

    @Inject
    private ImageRepository imageRepository;

    @Inject
    private GameRepository gameRepository;

    @Inject
    private GameNotificationRepository gameNotificationRepository;

    @Inject
    private ObjectiveRepository objectiveRepository;

    @Inject
    private ObjectiveService objectiveService;

    @Inject
    private ImageProviderService imageProviderService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GameNotificationResource gameNotificationResource;

    @Inject
    private GameNotificationService gameNotificationService;

    @Inject
    private DataExportService dataExportService;

    private final int MAX_DISTANCE = 64;

    private List<double[]> matchPointSets(final List<double[]> a, final List<double[]> b) {
        List<double[]> matchedPoints = new ArrayList<>();
        if(a != null && b != null && !a.isEmpty() && !b.isEmpty()) {
            List<double[]> referencePoints = new ArrayList<>(b);
            for (double[] p : a) {
                double d = MAX_DISTANCE*MAX_DISTANCE;
                double[] r = null;
                for (double[] q : referencePoints) {
                    Double dist = Math.pow(p[0] - q[0], 2) + Math.pow(p[1] - q[1], 2);
                    if (dist < d) {
                        log.debug("--------------Distance : {}-------------",dist);
                        d = dist;
                        r = q;
                    }
                }
                if (r != null) {
                    matchedPoints.add(new double[]{p[0],p[1]});
                    referencePoints.remove(r);
                }
            }
        }
        return matchedPoints;
    }

    private void awardPoints(Game game, int score) {
        Optional<User> playerRes = userRepository.findOneByLogin(game.getUsr());
        if(playerRes.get() != null) {
            User player = playerRes.get();
            int scoreGain = score - 50;
            if(scoreGain > 0 && player.getBonusPoints() > 0) {
                int bonusScore = Math.min(scoreGain,player.getBonusPoints());
                scoreGain += bonusScore;
                player.setBonusPoints(player.getBonusPoints() - bonusScore);
            }
            int newScore = player.getScore() + scoreGain;
            int newRank = player.getRank();
            if(newScore >= 100) {
                if(newRank == 1) {newScore = 100;}
                else {newScore-=100;newRank--;}
            }
            else if (newScore <= 0) {
                if(newRank == 50) {newScore = 0;}
                else {newScore+=100;newRank++;}
            }
            player.setScore(newScore);
            player.setRank(newRank);

            String gameId = game.getId();
            GameNotification gameNotification = new GameNotification();
            gameNotification.setTitle("Game reviewed!");
            gameNotification.setContent("You scored a " + score + "!");
            gameNotification.setUsr(player.getLogin());
            gameNotification.setGameId(gameId);
            gameNotificationRepository.save(gameNotification);

            objectiveRepository.findAllByUsr(player.getLogin()).stream().forEach(objective -> {
                List<String> pendingGames = objective.getPendingGames();
                if (pendingGames.contains(gameId)) {
                    pendingGames.remove(gameId);
                    objective.setBonusPoints(objective.getBonusPoints() + Math.max(score - 50, 0));
                    //Remove objectives when they do not contain any pending game and they have the required number of completed games.
                    if (objective.getNbGamesToComplete() == objective.getNbCompletedGames() && pendingGames.isEmpty()) {
                        player.setBonusPoints(player.getBonusPoints() + objective.getBonusPoints());
                        //Objective is completed, remove it from the database.
                        objectiveRepository.delete(objective);
                        //Add a notification informing the player he completed an objective.
                        GameNotification objectiveNotification = new GameNotification();
                        objectiveNotification.setTitle("Objective completed!");
                        objectiveNotification.setContent("You gained " + objective.getBonusPoints() + " bonus points!");
                        objectiveNotification.setUsr(player.getLogin());
                        gameNotificationRepository.save(objectiveNotification);
                    } else {
                        objectiveRepository.save(objective);
                    }
                    objectiveService.handleObjectiveUpdate(player.getLogin());
                }
            });
            userRepository.save(player);
            game.setLastModified(new DateTime());
            game.setScore(score);
            gameRepository.save(game);
            gameNotificationService.handleNewNotification(player.getLogin());
        }
    }

    public void handleExpertGameSubmission(ExpertGame expertGame) {
        ReferenceGame referenceGame = null;
        List<Game> games = null;
        switch(expertGame.getGameMode()) {
            case ExpertAllStars: {
                games = gameRepository.findAllByImgAndGameModeAndCompleted(expertGame.getImg(),GameMode.AllStars,true);
                Map<Species,Integer> referenceMap = ((AllStarsResult) expertGame.getSubmittedResult()).getSpeciesMap();

                Set<AnimalSpecies> faunaSpecies = new HashSet<>();
                Set<PlantSpecies> floraSpecies = new HashSet<>();

                for(Species key : referenceMap.keySet()) {
                    if (referenceMap.get(key) == 1) {
                        switch (key) {
                            case Batis:
                                floraSpecies.add(PlantSpecies.Batis);
                                break;
                            case Borrichia:
                                floraSpecies.add(PlantSpecies.Borrichia);
                                break;
                            case Juncus:
                                floraSpecies.add(PlantSpecies.Juncus);
                                break;
                            case Limonium:
                                floraSpecies.add(PlantSpecies.Limonium);
                                break;
                            case Salicornia:
                                floraSpecies.add(PlantSpecies.Salicornia);
                                break;
                            case Spartina:
                                floraSpecies.add(PlantSpecies.Spartina);
                                break;
                            case Burrow:
                                faunaSpecies.add(AnimalSpecies.Burrow);
                                break;
                            case Crab:
                                faunaSpecies.add(AnimalSpecies.Crab);
                                break;
                            case Mussel:
                                faunaSpecies.add(AnimalSpecies.Mussel);
                                break;
                            case Snail:
                                faunaSpecies.add(AnimalSpecies.Snail);
                                break;
                        }
                    }
                }

                for(Game game : games) {
                    Map<Species,Integer> submittedMap = ((AllStarsResult) game.getGameResult()).getSpeciesMap();
                    game.setCorrectedGameResult(expertGame.getSubmittedResult());

                    int mistakes = 0;
                    int correct = 0;
                    int total = 0;

                    for(Species key : referenceMap.keySet()) {
                        int r = referenceMap.get(key);
                        int s = submittedMap.get(key);
                        mistakes += (s != 0 && r != s) ? 1 : 0;
                        correct += r == s ? 1 : 0;
                        total++;
                    }

                    int score = (int) Math.floor(100 * Math.max(correct - mistakes, 0) / (total*1.));
                    //Award points to users that played on the image and add a notification to their notification queue.
                    game.setCorrectedGameResult(expertGame.getSubmittedResult());
                    awardPoints(game, score);
                }
                Image image = imageRepository.findOne(expertGame.getImg());
                image.getGameModes().remove(GameMode.ExpertAllStars);
                if(!faunaSpecies.isEmpty()) {
                    image.getGameModes().add(GameMode.AnimalIdentification);
                    image.setFaunaSpecies(faunaSpecies);
                }
                if(!floraSpecies.isEmpty()) {
                    image.getGameModes().add(GameMode.PlantIdentification);
                    image.setFloraSpecies(floraSpecies);
                }
                imageRepository.save(image);
            }
            break;
            case ExpertAnimalIdentification: {
                games = gameRepository.findAllByImgAndGameModeAndCompleted(expertGame.getImg(),GameMode.AnimalIdentification,true);
                Map<AnimalSpecies,List<double[]>> referenceMap = ((AnimalIdentificationResult) expertGame.getSubmittedResult()).getSpeciesMap();
                for(Game game : games) {
                    //Compute player's score
                    Map<AnimalSpecies,List<double[]>> submittedMap = ((AnimalIdentificationResult) game.getGameResult()).getSpeciesMap();

                    game.setCorrectedGameResult(expertGame.getSubmittedResult());

                    int mistakes = 0;
                    int correct = 0;
                    int total = 0;

                    for(AnimalSpecies key : submittedMap.keySet()) {
                        List<double[]> referenceList = referenceMap.get(key);
                        List<double[]> submittedList = submittedMap.get(key);
                        List<double[]> correctedList = matchPointSets(submittedList,referenceList);
                        mistakes += submittedList.size() - correctedList.size();
                        correct += correctedList.size();
                        total += referenceList.size();
                    }

                    int score = (int) Math.floor(100 * Math.max(correct - mistakes, 0) / (total*1.));
                    //Award points to users that played on the image and add a notification to their notification queue.
                    game.setCorrectedGameResult(expertGame.getSubmittedResult());
                    awardPoints(game,score);
                }
                Image image = imageRepository.findOne(expertGame.getImg());
                ImageModeStatus status = image.getModeStatus().get(ImageStatus.IN_PROCESSING).stream().filter(s->s.getMode().equals(GameMode.ExpertAnimalIdentification)).findFirst().get();
                image.getModeStatus().get(ImageStatus.IN_PROCESSING).remove(status);
                image.getModeStatus().get(ImageStatus.PROCESSED).add(status);

                if(!referenceMap.isEmpty()) {
                    referenceGame = new ReferenceGame<AnimalIdentificationResult>();
                    referenceGame.setGameResult(expertGame.getSubmittedResult());
                    referenceGame.setImg(expertGame.getImg());
                    referenceGame.setGameMode(GameMode.TrainingAnimalIdentification);
                    referenceGameRepository.save(referenceGame);
                    image.getGameModes().add(referenceGame.getGameMode());
                }

                imageRepository.save(image);
            }
            break;
            case ExpertPlantIdentification: {
                games = gameRepository.findAllByImgAndGameModeAndCompleted(expertGame.getImg(),GameMode.PlantIdentification,true);
                Map<PlantSpecies,List<Boolean>> referenceMap = ((PlantIdentificationResult) expertGame.getSubmittedResult()).getSpeciesMap();
                for(Game game : games) {
                    //Compute player's score
                    Map<PlantSpecies,List<Boolean>> submittedMap = ((PlantIdentificationResult) game.getGameResult()).getSpeciesMap();

                    game.setCorrectedGameResult(expertGame.getSubmittedResult());

                    int mistakes = 0;
                    int correct = 0;
                    int total = 0;

                    for(PlantSpecies key : submittedMap.keySet()) {
                        List<Boolean> referenceList = referenceMap.get(key);
                        List<Boolean> submittedList = submittedMap.get(key);
                        for(int i = 0; i<referenceList.size(); i++) {
                            mistakes += (referenceList.get(i) != submittedList.get(i)) ? 1 : 0;
                            correct += (referenceList.get(i) && submittedList.get(i)) ? 1 : 0;
                            total += referenceList.get(i) ? 1 : 0;
                        }
                    }

                    int score = (int) Math.floor(100 * Math.max(correct - mistakes, 0) / (total*1.));
                    //Award points to users that played on the image and add a notification to their notification queue.
                    game.setCorrectedGameResult(expertGame.getSubmittedResult());
                    awardPoints(game,score);
                }
                Image image = imageRepository.findOne(expertGame.getImg());
                image.getGameModes().remove(GameMode.ExpertPlantIdentification);

                if(!referenceMap.isEmpty()) {
                    referenceGame = new ReferenceGame<PlantIdentificationResult>();
                    referenceGame.setGameResult(expertGame.getSubmittedResult());
                    referenceGame.setImg(expertGame.getImg());
                    referenceGame.setGameMode(GameMode.TrainingPlantIdentification);
                    referenceGameRepository.save(referenceGame);
                    image.getGameModes().add(referenceGame.getGameMode());
                }

                imageRepository.save(image);
            }
            break;
        }
    }

    private ExpertGame setupExpertGame(ExpertGame expertGame, Game game, GameMode gameMode) {
        GameMode mode = game.getGameMode();
        String login = game.getUsr();
        expertGame.setCompleted(false);
        expertGame.setGameMode(mode);
        expertGame.setUsr(login);
        expertGame.setSubmittedResult(game.getGameResult());
        Image img = imageProviderService.findOne(mode, login);
        if(img == null)
            return null;
        ProcessedGame processedGame = processedGameRepository.findByImgAndGameMode(img.getId(), gameMode);
        if(processedGame != null)
            expertGame.setProcessedResult(processedGame.getProcessedGameResult());
        expertGame.setImg(img.getId());
        log.debug("Created game : {}", expertGame);
        expertGameRepository.save(expertGame);
        return expertGame;
    }

    public ExpertGame createExpertGame(Game game) {
        ExpertGame result = null;
        GameMode processedGameMode = null;
        switch(game.getGameMode()) {
            case ExpertAnimalIdentification: {
                result = new ExpertGame<ProcessedAnimalIdentificationResult, AnimalIdentificationResult>();
                processedGameMode = GameMode.AnimalIdentification;
            }
            break;
            case ExpertPlantIdentification: {
                result = new ExpertGame<ProcessedPlantIdentificationResult, PlantIdentificationResult>();
                processedGameMode = GameMode.PlantIdentification;
            }
            break;
            case ExpertAllStars: {
                result = new ExpertGame<ProcessedAllStarsResult,AllStarsResult>();
                processedGameMode = GameMode.AllStars;
            }
            default:
        }
        result = setupExpertGame(result,game,processedGameMode);
        return result;
    }
}
