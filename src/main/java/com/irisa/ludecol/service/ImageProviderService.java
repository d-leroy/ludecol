package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.ExpertGame;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.TrainingGame;
import com.irisa.ludecol.domain.subdomain.GameMode;
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

    private List<Image> filterPlayedImage(List<Image> images, GameMode mode, String login) {
        List<String> games = gameRepository.findAllByUsrAndGameMode(login, mode).stream().map(Game::getImg).collect(Collectors.toList());
        return images.stream()
            .filter(i -> !games.contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterPlayedTrainingImage(List<Image> images, GameMode mode, String login) {
        List<String> games = trainingGameRepository.findAllByUsrAndGameMode(login, mode).stream().map(TrainingGame::getImg).collect(Collectors.toList());
        return images.stream()
            .filter(i -> !games.contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterPlayedExpertImage(List<Image> images, GameMode mode, String login) {
        List<String> games = expertGameRepository.findAllByUsrAndGameMode(login, mode).stream().map(ExpertGame::getImg).collect(Collectors.toList());
        return images.stream()
            .filter(i -> !games.contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterImageList(List<Image> images, GameMode mode) {
        int maxPlayed = images.get(0).getModeStatus().get(mode).getGameNumber();
        return images.stream()
            .filter(i->i.getModeStatus().get(mode).getGameNumber() == maxPlayed)
            .collect(Collectors.toList());
    }

    /**
     * Finds one image among the images that have never been played by the {@code login} user on the {@code mode} game mode.
     * @param mode
     * @param login
     * @return
     */
    public Image findImage(GameMode mode, String login) {
        List<Image> images = mongoTemplate.find(query(where("mode_status." + mode + ".status").is(ImageStatus.NOT_PROCESSED.toString())), Image.class);
        images = filterPlayedImage(images, mode, login);
        images = images.stream()
            .sorted(Comparator.comparingInt(i -> i.getModeStatus().get(mode).getGameNumber()))
            .collect(Collectors.toList());
        if(images.isEmpty()) {
            images = mongoTemplate.find(query(where("mode_status." + mode + ".status").is(ImageStatus.IN_PROCESSING.toString())), Image.class);
            images = filterPlayedImage(images, mode, login);
            images = images.stream()
                .sorted(Comparator.comparingInt(i->i.getModeStatus().get(mode).getGameNumber()))
                .collect(Collectors.toList());
            if(images.isEmpty()) {
                images = mongoTemplate.find(query(where("mode_status." + mode + ".status").is(ImageStatus.PROCESSED.toString())), Image.class);
                images = filterPlayedImage(images, mode, login);
                if(images.isEmpty()) {
                    log.debug("No eligible image was found");
                    return null;
                }
            }
        }
        images = filterImageList(images, mode);
        log.debug("Total number of eligible images : {}", images.size());
        return images.get(rand.nextInt(images.size()));
    }

    public Image findTrainingImage(GameMode mode, String login) {
        List<Image> images = mongoTemplate.find(query(where("mode_status."+mode+".status").is(ImageStatus.PROCESSED.toString())), Image.class);
        images = filterPlayedTrainingImage(images, mode, login);
        images = images.stream()
            .sorted(Comparator.comparingInt(i -> i.getModeStatus().get(mode).getGameNumber()))
            .collect(Collectors.toList());
        if(images.isEmpty())
            return null;
        log.debug("Total number of eligible images : {}", images.size());
        return images.get(rand.nextInt(images.size()));
    }

    public Image findExpertImage(GameMode mode, String login) {
        List<Image> images = mongoTemplate.find(query(where("mode_status."+mode+".status").is(ImageStatus.IN_PROCESSING.toString())), Image.class);
        images = filterPlayedImage(images, mode, login);
        images = filterPlayedExpertImage(images, mode, login);
        images = images.stream()
            .sorted(Comparator.comparingInt(i -> i.getModeStatus().get(mode).getGameNumber()))
            .collect(Collectors.toList());
        if(images.isEmpty())
            return null;
        log.debug("Total number of eligible images : {}", images.size());
        return images.get(rand.nextInt(images.size()));
    }
}
