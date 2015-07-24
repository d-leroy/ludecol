package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.*;
import com.irisa.ludecol.domain.subdomain.AnimalIdentificationResult;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.GameResult;
import com.irisa.ludecol.domain.subdomain.PlantIdentificationResult;
import com.irisa.ludecol.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by dorian on 07/05/15.
 */
@Service
public class ImageProviderService {

    public class PlayedImage {
        String played_image;

        @Override
        public String toString() {
            return "PlayedImage{ played_images= '" + played_image + "'}";
        }
    }

    @Inject
    private MongoTemplate mongoTemplate;

    @Inject
    private ImageRepository imageRepository;

    @Inject
    private GameRepository gameRepository;
    @Inject
    private ReferenceGameRepository referenceGameRepository;

    @Inject
    private TrainingGameRepository trainingGameRepository;

    @Inject
    private ExpertGameRepository expertGameRepository;

    private final Logger log = LoggerFactory.getLogger(ImageProviderService.class);

    private Random rand = new Random();

    /**
     * Retrieves all images that have been played on the {@code mode} game mode by the {@code login} user.
     * @param mode
     * @param login
     * @return
     */
    public List<String> findPlayedImages(GameMode mode, String login) {
        Set<String> tmp = new HashSet<>();
        switch(mode) {
            case TrainingAnimalIdentification:
            case TrainingPlantIdentification: {
                List<TrainingGame> list = trainingGameRepository.findAllByUsrAndGameMode(login, mode);
                list.forEach(game -> tmp.add(game.getImg()));
            }
            break;
            case ExpertAnimalIdentification:
            case ExpertPlantIdentification: {
                List<ExpertGame> list = expertGameRepository.findAllByUsrAndGameMode(login,mode);
                list.forEach(game -> tmp.add(game.getImg()));
            }
            break;
            default: {
                List<Game> list = gameRepository.findAllByGameMode(mode);
                Map<String,Integer> imgMap = new HashMap<>();
                list.stream()
                    .forEach(game -> {
                        String k = game.getImg();
                        int nb = game.getUsr() != login ? 1 : 3;
                        if(imgMap.containsKey(k)) {
                            imgMap.put(k,imgMap.get(k)+nb);
                        }
                        else {
                            imgMap.put(k,nb);
                        }
                    });
                imgMap.forEach((k,v) -> {if(v >= 3) tmp.add(k);});
            }
        }
//        Aggregation agg = Aggregation.newAggregation(
//            Aggregation.match(Criteria.where("game_mode").is(mode).andOperator(Criteria.where("usr").is(login))),
//            Aggregation.project("img", "game_mode").andExclude("_id"),
//            Aggregation.group("game_mode").push("img").as("played_image"),
//            Aggregation.unwind("played_image")
//        );
//        String doc;
//        switch(GameMode.valueOf(mode)) {
//            case TrainingAnimalIdentification:
//            case TrainingPlantIdentification:
//                doc = "T_TRAINING_GAME";
//                break;
//            case ExpertAnimalIdentification:
//            case ExpertPlantIdentification:
//                doc = "T_EXPERT_GAME";
//                break;
//            default:
//                doc = "T_GAME";
//        }
//        AggregationResults<PlayedImage> results = mongoTemplate.aggregate(agg, doc, PlayedImage.class);
//        List<PlayedImage> mappedResult = results.getMappedResults();
//        List<String> res = new ArrayList<>();
//        mappedResult.forEach((s) -> res.add(s.played_image));
        log.debug("Images available to game mode {} : {}", mode, tmp);
        List<String> res = new ArrayList<>();
        res.addAll(tmp);
        return res;
    }

    /**
     * Finds one image among the images that have never been played by the {@code login} user on the {@code mode} game mode.
     * @param mode
     * @param login
     * @return
     */
    public Image findOne(GameMode mode, String login) {
        List<Image> images = imageRepository.findByGameModesContaining(mode);
        if(images.isEmpty()) {
            log.debug("No image were found");
            return null;
        }
        List<String> played_images = findPlayedImages(mode, login);
        images.removeIf(image -> played_images.contains(image.getId()));
        log.debug("Total number of eligible images : {}", images.size());
        if(images.isEmpty()) return null;
        if(mode.equals(GameMode.TrainingAnimalIdentification) || mode.equals(GameMode.TrainingPlantIdentification)) {
            images.sort((o1, o2) -> {
                ReferenceGame referenceGame1 = referenceGameRepository.findByImgAndGameMode(o1.getId(),mode);
                ReferenceGame referenceGame2 = referenceGameRepository.findByImgAndGameMode(o2.getId(),mode);
                if(mode.equals(GameMode.TrainingAnimalIdentification)) {
                    AnimalIdentificationResult animalIdentificationResult1 = (AnimalIdentificationResult) referenceGame1.getGameResult();
                    AnimalIdentificationResult animalIdentificationResult2 = (AnimalIdentificationResult) referenceGame2.getGameResult();
                    List<double[]> tmp = new ArrayList<>();
                    animalIdentificationResult1.getSpeciesMap().values().stream().forEach(doubles -> tmp.addAll(doubles));
                    int nb1 = tmp.size();
                    tmp.clear();
                    animalIdentificationResult2.getSpeciesMap().values().stream().forEach(doubles -> tmp.addAll(doubles));
                    int nb2 = tmp.size();
                    return nb1 - nb2;
                }
                else {
                    PlantIdentificationResult plantIdentificationResult1 = (PlantIdentificationResult) referenceGame1.getGameResult();
                    PlantIdentificationResult plantIdentificationResult2 = (PlantIdentificationResult) referenceGame2.getGameResult();
                    List<Boolean> tmp = new ArrayList<>();
                    plantIdentificationResult1.getSpeciesMap().values().stream().forEach(booleans -> tmp.addAll(booleans));
                    tmp.removeIf(aBoolean -> !aBoolean);
                    int nb1 = tmp.size();
                    tmp.clear();
                    plantIdentificationResult2.getSpeciesMap().values().stream().forEach(booleans -> tmp.addAll(booleans));
                    tmp.removeIf(aBoolean -> !aBoolean);
                    int nb2 = tmp.size();
                    return nb1 - nb2;
                }
            });
        }
        return images.get(0);
//        return images.get(rand.nextInt(images.size()));
    }
}
