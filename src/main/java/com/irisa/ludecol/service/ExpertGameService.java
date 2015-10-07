package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.ExpertGame;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.subdomain.*;
import com.irisa.ludecol.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dorian on 07/05/15.
 */
@Service
public class ExpertGameService {

    private final Logger log = LoggerFactory.getLogger(ExpertGameService.class);

    @Inject
    private ExpertGameRepository expertGameRepository;

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
    private GameNotificationService gameNotificationService;

    @Inject
    private GameProcessingService gameProcessingService;

    @Inject
    private DataExportService dataExportService;

    public void handleExpertGameSubmission(ExpertGame expertGame) {
        Image image = imageRepository.findOne(expertGame.getImg());
        GameMode mode = expertGame.getGameMode();
        Map result = null;
        switch(mode) {
            case AllStars:
                result = ((AllStarsResult) expertGame.getSubmittedResult()).getSpeciesMap();
                break;
            case AnimalIdentification:
                result = ((AnimalIdentificationResult) expertGame.getSubmittedResult()).getSpeciesMap();
                break;
            case PlantIdentification:
                result = ((PlantIdentificationResult) expertGame.getSubmittedResult()).getSpeciesMap();
                break;
        }
        gameProcessingService.handleGameProcessing(image,mode,result);
    }

    private ExpertGame createNewExpertGame(GameMode mode) {
        ExpertGame result = null;
        switch (mode) {
            case AnimalIdentification: {
                result = new ExpertGame<AnimalIdentificationResult>();
            }
            break;
            case PlantIdentification: {
                result = new ExpertGame<PlantIdentificationResult>();
            }
            break;
            case AllStars: {
                result = new ExpertGame<AllStarsResult>();
            }
        }
        return result;
    }

    private GameResult createGameResult(GameMode mode, Map map) {
        GameResult result = null;
        switch (mode) {
            case AnimalIdentification: {
                result = new AnimalIdentificationResult();
                ((AnimalIdentificationResult) result).setSpeciesMap(map);
            }
            break;
            case PlantIdentification: {
                result = new PlantIdentificationResult();
                ((PlantIdentificationResult) result).setSpeciesMap(map);
            }
            break;
            case AllStars: {
                result = new AllStarsResult();
                ((AllStarsResult) result).setSpeciesMap(map);
            }
        }
        return result;
    }

    public ExpertGame createExpertGame(Game game) {
        GameMode mode = game.getGameMode();
        String login = game.getUsr();
        String img = game.getImg();
        ExpertGame result = null;
        if(img != null) {
            log.debug("Creating expert game on predefined image : {}", game.getImg());
            Image image = imageRepository.findOne(img);
            ImageModeStatus modeStatus = image.getModeStatus().get(mode);
            List<ExpertGame> games = expertGameRepository.findAllByImgAndUsrAndGameMode(img, login, mode);
            if (games.isEmpty()) {
                result = createNewExpertGame(mode);
            } else {
                result = games.get(0);
            }
//            Map refMap = modeStatus.getReferenceResult();
//            if(refMap != null) {
//                result.setReferenceResult(createGameResult(mode, refMap));
//            }
            result.setImg(img);
        } else {
            result = createNewExpertGame(mode);
            Image image = imageProviderService.findExpertImage(mode, login);
            if(image == null) {
                return null;
            }
            result.setImg(image.getId());
        }
        result.setSubmittedResult(game.getGameResult());
        result.setCompleted(false);
        result.setGameMode(mode);
        result.setUsr(login);

        return expertGameRepository.save(result);
    }
}
