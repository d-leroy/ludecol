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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        switch(expertGame.getGameMode()) {
            case AllStars: {
                Map<Species,Boolean> referenceMap = ((AllStarsResult) expertGame.getSubmittedResult()).getSpeciesMap();

                Set<AnimalSpecies> faunaSpecies = new HashSet<>();
                Set<PlantSpecies> floraSpecies = new HashSet<>();

                referenceMap.keySet().stream().filter(k->referenceMap.get(k)).forEach(k->{
                    try {floraSpecies.add(PlantSpecies.valueOf(k.toString()));}
                    catch (IllegalArgumentException e1) {
                        try {faunaSpecies.add(AnimalSpecies.valueOf(k.toString()));}
                        catch (IllegalArgumentException e2) {
                            e1.printStackTrace();
                            e2.printStackTrace();
                        }
                    }
                });

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

                imageRepository.save(image);
            }
            break;
            case AnimalIdentification: {
                Map<AnimalSpecies,List<double[]>> referenceMap = ((AnimalIdentificationResult) expertGame.getSubmittedResult()).getSpeciesMap();

                image.getModeStatus().get(GameMode.AnimalIdentification).setStatus(ImageStatus.PROCESSED);
                Set<AnimalSpecies> set = new HashSet<>();
                referenceMap.entrySet().stream().forEach(e -> {
                    if (!e.getValue().isEmpty())
                        set.add(e.getKey());
                });
                image.setFaunaSpecies(set);

                imageRepository.save(image);
            }
            break;
            case PlantIdentification: {
                Map<PlantSpecies,List<Boolean>> referenceMap = ((PlantIdentificationResult) expertGame.getSubmittedResult()).getSpeciesMap();

                image.getModeStatus().get(GameMode.PlantIdentification).setStatus(ImageStatus.PROCESSED);
                Set<PlantSpecies> set = new HashSet<>();
                referenceMap.entrySet().stream().forEach(e -> {
                    if (!e.getValue().contains(true))
                        set.add(e.getKey());
                });
                image.setFloraSpecies(set);

                imageRepository.save(image);
            }
            break;
        }
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
                switch (mode) {
                    case AnimalIdentification: {
                        result = new ExpertGame<AnimalIdentificationResult>();
                        AnimalIdentificationResult gameResult = new AnimalIdentificationResult();
                        if(modeStatus != null) {
                            gameResult.setSpeciesMap(modeStatus.getReferenceResult());
                        }
                        result.setProcessedResult(gameResult);
                    }
                    break;
                    case PlantIdentification: {
                        result = new ExpertGame<PlantIdentificationResult>();
                        PlantIdentificationResult gameResult = new PlantIdentificationResult();
                        if(modeStatus != null) {
                            gameResult.setSpeciesMap(modeStatus.getReferenceResult());
                        }
                        result.setProcessedResult(gameResult);
                    }
                    break;
                    case AllStars: {
                        result = new ExpertGame<AllStarsResult>();
                        AllStarsResult gameResult = new AllStarsResult();
                        if(modeStatus != null) {
                            gameResult.setSpeciesMap(modeStatus.getReferenceResult());
                        }
                        result.setProcessedResult(gameResult);
                    }
                    default:
                }
            } else {
                result = games.get(0);
                switch (mode) {
                    case AnimalIdentification: {
                        AnimalIdentificationResult gameResult = new AnimalIdentificationResult();
                        if(modeStatus != null) {
                            gameResult.setSpeciesMap(modeStatus.getReferenceResult());
                        }
                        result.setProcessedResult(gameResult);
                    }
                    break;
                    case PlantIdentification: {
                        PlantIdentificationResult gameResult = new PlantIdentificationResult();
                        if(modeStatus != null) {
                            gameResult.setSpeciesMap(modeStatus.getReferenceResult());
                        }
                        result.setProcessedResult(gameResult);
                    }
                    break;
                    case AllStars: {
                        AllStarsResult gameResult = new AllStarsResult();
                        if(modeStatus != null) {
                            gameResult.setSpeciesMap(modeStatus.getReferenceResult());
                        }
                        result.setProcessedResult(gameResult);
                    }
                    default:
                }
            }
            result.setImg(img);
        }
        else {
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
                default:
            }

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

        expertGameRepository.save(result);

        return result;
    }
}
