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

    @Inject
    private MongoTemplate mongoTemplate;

    @Inject
    private GameRepository gameRepository;

    @Inject
    private TrainingGameRepository trainingGameRepository;

    @Inject
    private ExpertGameRepository expertGameRepository;

    private final Logger log = LoggerFactory.getLogger(ImageProviderService.class);

    private Random rand = new Random();

    private List<Image> filterPlayedImages(List<Image> images, GameMode mode, String login) {
        List<String> games = gameRepository.findAllByUsrAndGameMode(login, mode).stream().map(Game::getImg).collect(Collectors.toList());
        return images.stream()
            .filter(i -> !games.contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterPlayedTrainingImages(List<Image> images, GameMode mode, String login) {
        List<String> games = trainingGameRepository.findAllByUsrAndGameMode(login, mode).stream().map(TrainingGame::getImg).collect(Collectors.toList());
        return images.stream()
            .filter(i -> !games.contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterPlayedExpertImages(List<Image> images, GameMode mode, String login) {
        List<String> games = expertGameRepository.findAllByUsrAndGameMode(login, mode).stream().map(ExpertGame::getImg).collect(Collectors.toList());
        return images.stream()
            .filter(i -> !games.contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterImageListByGameNumber(List<Image> images, GameMode mode) {
        int maxPlayed = images.get(0).getModeStatus().get(mode).getGameNumber();
        return images.stream()
            .filter(i->i.getModeStatus().get(mode).getGameNumber() == maxPlayed)
            .collect(Collectors.toList());
    }

    private List<Image> filterImageListBySet(List<Image> images) {
        int maxSetPriority = images.stream().collect(Collectors.maxBy(Comparator.comparingInt(Image::getSetPriority))).get().getSetPriority();
        return images.stream().filter(i->i.getSetPriority() == maxSetPriority).collect(Collectors.toList());
    }

    /**
     * Finds one image among the images that have never been played by the {@code login} user on the {@code mode} game mode.
     * @param mode
     * @param login
     * @return
     */
    public Image findImage(GameMode mode, String login) {
        List<Image> images = mongoTemplate.find(query(where("mode_status." + mode + ".status").is(ImageStatus.NOT_PROCESSED.toString())), Image.class);
        images = filterPlayedImages(images, mode, login);
        images = images.stream()
            .sorted(new Comparator<Image>() {
                @Override
                public int compare(Image i1, Image i2) {
                    GameMode altMode;
                    switch(mode) {
                        case AnimalIdentification: altMode = GameMode.PlantIdentification; break;
                        case PlantIdentification: altMode = GameMode.AnimalIdentification; break;
                        default: altMode = null;
                    }
                    if(altMode != null) {
                        ImageModeStatus a1 = i1.getModeStatus().get(altMode);
                        ImageModeStatus a2 = i2.getModeStatus().get(altMode);
                        switch(a1.getStatus()) {
                            case IN_PROCESSING:
                            case PROCESSED:
                            case UNAVAILABLE:
                                switch(a2.getStatus()) {
                                    case NOT_PROCESSED:
                                        return 1;
                                }
                                break;
                            case NOT_PROCESSED:
                                switch(a2.getStatus()) {
                                    case IN_PROCESSING:
                                    case PROCESSED:
                                    case UNAVAILABLE:
                                        return -1;
                                }
                                break;
                        }
                    }
                    ImageModeStatus m1 = i1.getModeStatus().get(mode);
                    ImageModeStatus m2 = i2.getModeStatus().get(mode);
                    return Integer.compare(m1.getGameNumber(), m2.getGameNumber());
                }
            })
            .sorted(Comparator.comparingInt(i -> i.getModeStatus().get(mode).getGameNumber()))
            .collect(Collectors.toList());
        if(images.isEmpty()) {
            images = mongoTemplate.find(query(where("mode_status." + mode + ".status").is(ImageStatus.IN_PROCESSING.toString())), Image.class);
            images = filterPlayedImages(images, mode, login);
            images = images.stream()
                .sorted(Comparator.comparingInt(i->i.getModeStatus().get(mode).getGameNumber()))
                .collect(Collectors.toList());
            if(images.isEmpty()) {
                images = mongoTemplate.find(query(where("mode_status." + mode + ".status").is(ImageStatus.PROCESSED.toString())), Image.class);
                images = filterPlayedImages(images, mode, login);
                if(images.isEmpty()) {
                    log.debug("No eligible image was found");
                    return null;
                }
            }
        }
        images = filterImageListBySet(images);
        images = filterImageListByGameNumber(images, mode);
        log.debug("Total number of eligible images : {}", images.size());
        if(images.isEmpty())
            return null;
        return images.get(rand.nextInt(images.size()));
    }

    /**
     * Finds one image among the images that have never been played by the {@code login} user on the {@code mode} training game mode.
     * @param mode
     * @param login
     * @return
     */
    public Image findTrainingImage(GameMode mode, String login) {
        //Eligible images are in the PROCESSED status for the requested mode.
        List<Image> images = mongoTemplate.find(query(where("mode_status."+mode+".status").is(ImageStatus.PROCESSED.toString())), Image.class);
        //Eligible images have not already been played by the requesting player.
        images = filterPlayedTrainingImages(images, mode, login);
        images = images.stream()
            .sorted(Comparator.comparingInt(i -> i.getModeStatus().get(mode).getGameNumber()))
            .collect(Collectors.toList());
        if(images.isEmpty())
            return null;
        log.debug("Total number of eligible images : {}", images.size());
        return images.get(rand.nextInt(images.size()));
    }

    /**
     * Finds one image among the images that have never been played by the {@code login} user on the {@code mode} expert game mode.
     * @param mode
     * @param login
     * @return
     */
    public Image findExpertImage(GameMode mode, String login) {
        //Eligible images are in the IN_PROCESSING status for the requested mode.
        List<Image> images = mongoTemplate.find(query(where("mode_status."+mode+".status").is(ImageStatus.IN_PROCESSING.toString())), Image.class);
        //Eligible images have not laready been played by the requesting player, on nomal as well as on expert level.
        images = filterPlayedImages(images, mode, login);
        images = filterPlayedExpertImages(images, mode, login);
        images = images.stream()
            .sorted(Comparator.comparingInt(i -> i.getModeStatus().get(mode).getGameNumber()))
            .collect(Collectors.toList());
        if(images.isEmpty())
            return null;
        images = filterImageListBySet(images);
        images = filterImageListByGameNumber(images, mode);
        log.debug("Total number of eligible images : {}", images.size());
        return images.get(rand.nextInt(images.size()));
    }
}
