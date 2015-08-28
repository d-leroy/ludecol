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
    private GameNotificationService gameNotificationService;

    @Inject
    private GameProcessingService gameProcessingService;

    @Inject
    private DataExportService dataExportService;

    public void handleExpertGameSubmission(ExpertGame expertGame) {
        ReferenceGame referenceGame = null;
        switch(expertGame.getGameMode()) {
            case AllStars: {
                Map<Species,Integer> referenceMap = ((AllStarsResult) expertGame.getSubmittedResult()).getSpeciesMap();

                Set<AnimalSpecies> faunaSpecies = new HashSet<>();
                Set<PlantSpecies> floraSpecies = new HashSet<>();

                referenceMap.keySet().stream().filter(k->referenceMap.get(k) == 1).forEach(k->{
                    try {floraSpecies.add(PlantSpecies.valueOf(k.toString()));}
                    catch (IllegalArgumentException e1) {
                        try {faunaSpecies.add(AnimalSpecies.valueOf(k.toString()));}
                        catch (IllegalArgumentException e2) {
                            e1.printStackTrace();
                            e2.printStackTrace();
                        }
                    }
                });

                Image image = imageRepository.findOne(expertGame.getImg());
                image.getModeStatus().get(GameMode.AllStars).setStatus(ImageStatus.PROCESSED);
                if(!faunaSpecies.isEmpty()) {
                    ImageModeStatus modeStatus = image.getModeStatus().get(GameMode.AnimalIdentification);
                    if(modeStatus.getStatus().equals(ImageStatus.UNAVAILABLE))
                        modeStatus.setStatus(ImageStatus.NOT_PROCESSED);
                    image.setFaunaSpecies(faunaSpecies);
                }
                if(!floraSpecies.isEmpty()) {
                    ImageModeStatus modeStatus = image.getModeStatus().get(GameMode.PlantIdentification);
                    if(modeStatus.getStatus().equals(ImageStatus.UNAVAILABLE))
                        modeStatus.setStatus(ImageStatus.NOT_PROCESSED);
                    image.setFloraSpecies(floraSpecies);
                }

                if(!referenceMap.isEmpty())
                    referenceGame = new ReferenceGame<AllStarsResult>();

                imageRepository.save(image);
            }
            break;
            case AnimalIdentification: {
                Map<AnimalSpecies,List<double[]>> referenceMap = ((AnimalIdentificationResult) expertGame.getSubmittedResult()).getSpeciesMap();
                Image image = imageRepository.findOne(expertGame.getImg());

                image.getModeStatus().get(GameMode.AnimalIdentification).setStatus(ImageStatus.PROCESSED);
                Set<AnimalSpecies> set = new HashSet<>();
                referenceMap.entrySet().stream().forEach(e -> {
                    if (!e.getValue().isEmpty())
                        set.add(e.getKey());
                });
                image.setFaunaSpecies(set);

                if(!referenceMap.isEmpty())
                    referenceGame = new ReferenceGame<AnimalIdentificationResult>();

                imageRepository.save(image);
            }
            break;
            case PlantIdentification: {
                Map<PlantSpecies,List<Boolean>> referenceMap = ((PlantIdentificationResult) expertGame.getSubmittedResult()).getSpeciesMap();
                Image image = imageRepository.findOne(expertGame.getImg());

                image.getModeStatus().get(GameMode.PlantIdentification).setStatus(ImageStatus.PROCESSED);
                Set<PlantSpecies> set = new HashSet<>();
                referenceMap.entrySet().stream().forEach(e -> {
                    if (!e.getValue().contains(true))
                        set.add(e.getKey());
                });
                image.setFloraSpecies(set);

                if(!referenceMap.isEmpty())
                    referenceGame = new ReferenceGame<PlantIdentificationResult>();

                imageRepository.save(image);
            }
            break;
        }
        referenceGame.setGameResult(expertGame.getSubmittedResult());
        referenceGame.setImg(expertGame.getImg());
        referenceGame.setGameMode(expertGame.getGameMode());
        referenceGameRepository.save(referenceGame);


        List<Game> games = gameRepository.findAllByImgAndGameModeAndCompleted(expertGame.getImg(),expertGame.getGameMode(),true);
        for(Game game : games) {
            game.setCorrectedGameResult(expertGame.getSubmittedResult());
            gameProcessingService.rateGame(game);
        }
    }

    private ExpertGame setupExpertGame(ExpertGame expertGame, Game game) {
        GameMode mode = game.getGameMode();
        String login = game.getUsr();
        expertGame.setCompleted(false);
        expertGame.setGameMode(mode);
        expertGame.setUsr(login);
        expertGame.setSubmittedResult(game.getGameResult());
        Image img = imageProviderService.findExpertImage(mode, login);
        if (img == null)
            return null;
        ProcessedGame processedGame = processedGameRepository.findByImgAndGameMode(img.getId(), mode);
        if(processedGame != null)
            expertGame.setProcessedResult(processedGame.getProcessedGameResult());
        expertGame.setImg(img.getId());
        log.debug("Created game : {}", expertGame);
        expertGameRepository.save(expertGame);
        return expertGame;
    }

    public ExpertGame createExpertGame(Game game) {
        ExpertGame result = null;
        if(game.getImg() != null) {
            log.debug("Creating expert game on predefined image : {}", game.getImg());
            List<ExpertGame> games = expertGameRepository.findAllByImgAndUsrAndGameMode(game.getImg(), game.getUsr(), game.getGameMode());
            if (!games.isEmpty()) {
                result = games.get(0);
                result.setCompleted(false);
                result.setGameMode(game.getGameMode());
                result.setUsr(game.getUsr());
                result.setSubmittedResult(game.getGameResult());
//                result.setProcessedResult(); TODO: convertir le reference result actuel de l'image en processed result.
            }
            else {
                result = new ExpertGame<ProcessedAllStarsResult, AllStarsResult>();
                result.setCompleted(false);
                result.setGameMode(game.getGameMode());
                result.setUsr(game.getUsr());
                result.setSubmittedResult(game.getGameResult());
                result.setImg(game.getImg());
                ProcessedGame processedGame = processedGameRepository.findByImgAndGameMode(game.getImg(), game.getGameMode());
                if(processedGame != null)
                    result.setProcessedResult(processedGame.getProcessedGameResult());
            }
            log.debug("Created game : {}", result);
            expertGameRepository.save(result);
        }
        else {
            switch (game.getGameMode()) {
                case AnimalIdentification: {
                    result = new ExpertGame<ProcessedAnimalIdentificationResult, AnimalIdentificationResult>();
                }
                break;
                case PlantIdentification: {
                    result = new ExpertGame<ProcessedPlantIdentificationResult, PlantIdentificationResult>();
                }
                break;
                case AllStars: {
                    result = new ExpertGame<ProcessedAllStarsResult, AllStarsResult>();
                }
                default:
            }
            result = setupExpertGame(result, game);
        }
        return result;
    }
}
