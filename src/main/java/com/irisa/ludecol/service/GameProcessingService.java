package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.ProcessedGame;
import com.irisa.ludecol.domain.User;
import com.irisa.ludecol.domain.subdomain.*;
import com.irisa.ludecol.repository.ImageRepository;
import com.irisa.ludecol.repository.ObjectiveRepository;
import com.irisa.ludecol.repository.ProcessedGameRepository;
import com.irisa.ludecol.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dorian on 26/05/15.
 */
@Service
public class GameProcessingService {

    private final Logger log = LoggerFactory.getLogger(GameProcessingService.class);

    private final int MIN_RESULT = 1;

    @Inject
    private ProcessedGameRepository processedGameRepository;

    @Inject
    private ImageRepository imageRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ObjectiveRepository objectiveRepository;

    @Inject
    private ObjectiveService objectiveService;


    private void processPresenceGrid(final List<Boolean> inputData, final List<Double> processedData, int n) {
        if(processedData.isEmpty()) {
            inputData.forEach(b -> processedData.add(b ? 1. : 0.));
        }
        else {
            for (int i = 0; i < inputData.size(); i++) {
                processedData.set(i,processedData.get(i) * n + (inputData.get(i) ? 1 : 0) / ((double) (n+1)));
            }
        }
    }

    private void processPlantIdentification(final PlantIdentificationResult gameResult, final ProcessedPlantIdentificationResult processedResult) {
        Map<PlantSpecies,List<Boolean>> submittedMap = gameResult.getSpeciesMap();
        Map<PlantSpecies,List<Double>> processedMap = processedResult.getSpeciesMap();

        for(PlantSpecies key : submittedMap.keySet()) {
            List<Double> processedList = processedMap.get(key);
            if(processedList == null) {
                processedList = new ArrayList<>();
                processedMap.put(key,processedList);
            }
            processPresenceGrid(submittedMap.get(key), processedList, processedResult.getNbResults());
        }

        processedResult.setNbResults(processedResult.getNbResults()+1);
    }

    private void processAllStars(final AllStarsResult gameResult, final ProcessedAllStarsResult processedResult) {
        Map<Species,Integer> submittedMap = gameResult.getSpeciesMap();
        Map<Species,Pair<Double>> processedMap = processedResult.getSpeciesMap();
        int n = processedResult.getNbResults();

        for(Species key : submittedMap.keySet()) {
            Pair<Double> processedPair = processedMap.get(key);
            if(processedPair == null) {
                processedPair = new Pair<>(0.,0.);
                processedMap.put(key, processedPair);
            }
            int val = submittedMap.get(key);
            switch(val) {
                case -1:
                    processedPair.setY((processedPair.getY() * n + 1.) / (n+1.));
                    break;
                case 0: break;
                case 1:
                    processedPair.setX((processedPair.getX() * n + 1.) / (n+1.));
                    break;
                default:
            }
        }

        processedResult.setNbResults(n+1);
    }

    private void processAnimalIdentification(final AnimalIdentificationResult gameResult, final ProcessedAnimalIdentificationResult processedResult) {
        Map<AnimalSpecies,List<double[]>> submittedMap = gameResult.getSpeciesMap();
        Map<AnimalSpecies,List<double[]>> processedMap = processedResult.getSpeciesMap();

        submittedMap.keySet().stream().forEach(key -> {
            List<double[]> processedList = processedMap.get(key);
            if (processedList == null) {
                processedMap.put(key, submittedMap.get(key));
            } else {
                processedList.addAll(submittedMap.get(key));
            }
        });

        processedResult.setNbResults(processedResult.getNbResults()+1);
    }

    private void configureProcessedGame(ProcessedGame processedGame, ProcessedGameResult result, Game game) {
        processedGame.setImg(game.getImg());
        processedGame.setGameMode(game.getGameMode());
        processedGame.setProcessedGameResult(result);
    }

    public void processGame(Game game) {
        User player = userRepository.findOneByLogin(game.getUsr()).get();
        if(player == null) return;
        objectiveRepository.findAllByUsr(player.getLogin()).stream()
            .filter(o -> o.getGameMode() == game.getGameMode() && o.getNbCompletedGames() < o.getNbGamesToComplete())
            .sorted((o1, o2) -> (int) Math.signum(o1.getCreationDate().getMillis() - o2.getCreationDate().getMillis()))
            .findFirst().ifPresent(objective -> {
                objective.getPendingGames().add(game.getId());
                objective.setNbCompletedGames(objective.getNbCompletedGames() + 1);
                objectiveRepository.save(objective);
            });
        userRepository.save(player);
        objectiveService.handleObjectiveUpdate(player.getLogin());

        ProcessedGame processedGame = processedGameRepository.findByImgAndGameMode(game.getImg(), game.getGameMode());
        switch(game.getGameMode()) {
            case PlantIdentification: {
                if(processedGame == null) {
                    processedGame = new ProcessedGame();
                    ProcessedPlantIdentificationResult processedGameResult = new ProcessedPlantIdentificationResult();
                    configureProcessedGame(processedGame,processedGameResult,game);
                }
                processPlantIdentification((PlantIdentificationResult) game.getGameResult(),
                    (ProcessedPlantIdentificationResult) processedGame.getProcessedGameResult());
                log.debug("Processed game : {}", processedGame);
                processedGameRepository.save(processedGame);
                if(processedGame.getProcessedGameResult().getNbResults() >= MIN_RESULT) {
                    Image img = imageRepository.findOne(game.getImg());
                    ImageModeStatus modeStatus = img.getModeStatus().get(GameMode.PlantIdentification);
                    if(modeStatus.getStatus().equals(ImageStatus.NOT_PROCESSED)) {
                        modeStatus.setStatus(ImageStatus.IN_PROCESSING);
                        imageRepository.save(img);
                    }
                }
            }
            break;
            case AnimalIdentification: {
                if(processedGame == null) {
                    processedGame = new ProcessedGame();
                    ProcessedAnimalIdentificationResult processedGameResult = new ProcessedAnimalIdentificationResult();
                    configureProcessedGame(processedGame,processedGameResult,game);
                }
                processAnimalIdentification((AnimalIdentificationResult) game.getGameResult(),
                    (ProcessedAnimalIdentificationResult) processedGame.getProcessedGameResult());
                log.debug("Processed game : {}", processedGame);
                processedGameRepository.save(processedGame);
                if(processedGame.getProcessedGameResult().getNbResults() >= MIN_RESULT) {
                    Image img = imageRepository.findOne(game.getImg());
                    ImageModeStatus modeStatus = img.getModeStatus().get(GameMode.AnimalIdentification);
                    if(modeStatus.getStatus().equals(ImageStatus.NOT_PROCESSED)) {
                        modeStatus.setStatus(ImageStatus.IN_PROCESSING);
                        imageRepository.save(img);
                    }
                }
            }
            break;
            case AllStars: {
                if(processedGame == null) {
                    processedGame = new ProcessedGame();
                    ProcessedAllStarsResult processedGameResult = new ProcessedAllStarsResult();
                    configureProcessedGame(processedGame,processedGameResult,game);
                }
                processAllStars((AllStarsResult) game.getGameResult(),
                    (ProcessedAllStarsResult) processedGame.getProcessedGameResult());
                log.debug("Processed game : {}", processedGame);
                processedGameRepository.save(processedGame);
                if(processedGame.getProcessedGameResult().getNbResults() >= MIN_RESULT) {
                    Image img = imageRepository.findOne(game.getImg());
                    ImageModeStatus modeStatus = img.getModeStatus().get(GameMode.AllStars);
                    if(modeStatus.getStatus().equals(ImageStatus.NOT_PROCESSED)) {
                        modeStatus.setStatus(ImageStatus.IN_PROCESSING);
                        imageRepository.save(img);
                    }
                }
            }
            break;
        }
    }
}
