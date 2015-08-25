package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.ExpertGame;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.TrainingGame;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageModeStatus;
import com.irisa.ludecol.domain.subdomain.ImageStatus;
import com.irisa.ludecol.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

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
    private ImageService imageService;

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

    public List<String> findImagesToPlay(GameMode mode, String login) {

        Map<String,Integer> imgs = imageService.getImagesNumberOfGames(
            gameRepository.findAllByGameMode(mode).stream()
                //Remove images that were already played by the current user.
                .filter(g->!g.getUsr().equals(login)).collect(Collectors.toList()));
        return imgs.entrySet().stream()
            //Remove images that have been sufficiently played to be processed.
            .filter(e->e.getValue()<3)
            //Sort images by the number of times they have been played
            .sorted(Map.Entry.comparingByValue())
            //Extracts the list of images
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all images that have been played on the {@code mode} game mode by the {@code login} user
     * or other players depending on the game mode.
     * TODO: prevent an expert player to play on an image he already played in normal mode.
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
                        int nb = game.getUsr().equals(login) ? 3 : 1;
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

        log.debug("Images already played in game mode {} : {}", mode, tmp);
        List<String> res = new ArrayList<>();
        res.addAll(tmp);
        return res;
    }

    private List<Image> filterPlayedImage(List<Image> images, GameMode mode, String login) {
        return images.stream()
            .filter(i -> !gameRepository.findAllByUsrAndGameMode(login, mode).stream().map(Game::getImg).collect(Collectors.toList()).contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterImageList(List<Image> images, GameMode mode, ImageStatus status) {
        int maxPlayed = images.get(0).getModeStatus().get(status).stream().filter(s -> s.getMode().equals(mode)).findFirst().get().getGameNumber();
        return images.stream()
            .filter(i->i.getModeStatus().get(status).stream().filter(s -> s.getMode().equals(mode)).findFirst().get().getGameNumber() == maxPlayed)
            .collect(Collectors.toList());
    }

    /**
     * Finds one image among the images that have never been played by the {@code login} user on the {@code mode} game mode.
     * @param mode
     * @param login
     * @return
     */
    public Image findOne(GameMode mode, String login) {
        List<Image> images = mongoTemplate.find(query(where("mode_status.NOT_PROCESSED").elemMatch(where("mode").is(mode.toString()))), Image.class);
        images = filterPlayedImage(images, mode, login);
        images = images.stream()
            .sorted(Comparator.comparingInt(i -> i.getModeStatus().get(ImageStatus.NOT_PROCESSED).stream().filter(s -> s.getMode().equals(mode)).findFirst().get().getGameNumber()))
            .collect(Collectors.toList());
        if(images.isEmpty()) {
            images = mongoTemplate.find(query(where("mode_status.IN_PROCESSING").elemMatch(where("mode").is(mode.toString()))), Image.class);
            images = filterPlayedImage(images, mode, login);
            images = images.stream()
                .sorted(Comparator.comparingInt(i->i.getModeStatus().get(ImageStatus.IN_PROCESSING).stream().filter(s->s.getMode().equals(mode)).findFirst().get().getGameNumber()))
                .collect(Collectors.toList());
            if(images.isEmpty()) {
                images = mongoTemplate.find(query(where("mode_status.PROCESSED").elemMatch(where("mode").is(mode.toString()))), Image.class);
                images = filterPlayedImage(images, mode, login);
                if(images.isEmpty()) {
                    log.debug("No eligible image was found");
                    return null;
                }
            }
            else images = filterImageList(images, mode, ImageStatus.IN_PROCESSING);
        }
        else images = filterImageList(images, mode, ImageStatus.NOT_PROCESSED);
        Collections.shuffle(images);
        log.debug("Total number of eligible images : {}", images.size());
        return images.get(0);
    }
}
