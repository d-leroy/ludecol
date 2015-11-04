package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.*;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageModeStatus;
import com.irisa.ludecol.domain.subdomain.ImageStatus;
import com.irisa.ludecol.domain.subdomain.Pair;
import com.irisa.ludecol.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private UserRepository userRepository;

    @Inject
    private GameRepository gameRepository;

    @Inject
    private TrainingGameRepository trainingGameRepository;

    @Inject
    private ExpertGameRepository expertGameRepository;

    @Inject
    private ImageSetRepository imageSetRepository;

    private final Logger log = LoggerFactory.getLogger(ImageProviderService.class);

    private Random rand = new Random();

    private List<Image> filterEmptyImages(List<Image> images, GameMode mode) {
        List<Image> result = images;
        switch(mode) {
            case AnimalIdentification:
                result = images.stream().filter(i-> {
                    Map<?, List> refResult = i.getModeStatus().get(mode).getReferenceResult();
                    return refResult != null && refResult.values().stream().anyMatch(l->!l.isEmpty());
                }).collect(Collectors.toList());
                break;
            case PlantIdentification:
                result = images.stream().filter(i-> {
                    Map<?, List<Boolean>> refResult = i.getModeStatus().get(mode).getReferenceResult();
                    return refResult != null && refResult.values().stream().anyMatch(l->l.stream().anyMatch(b->b));
                }).collect(Collectors.toList());
                break;
        }
        return result;
    }

    private List<Image> filterPlayedImages(List<Image> images, GameMode mode, String login) {
        List<String> games = gameRepository.findAllByUsrAndGameMode(login, mode).stream()
            .map(Game::getImg)
            .collect(Collectors.toList());
        return images.stream()
            .filter(i -> !games.contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterPlayedTrainingImages(List<Image> images, GameMode mode, String login) {
        List<String> games = trainingGameRepository.findAllByUsrAndGameMode(login, mode).stream()
            .map(TrainingGame::getImg)
            .collect(Collectors.toList());
        return images.stream()
            .filter(i -> !games.contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterPlayedExpertImages(List<Image> images, GameMode mode, String login) {
        List<String> games = expertGameRepository.findAllByUsrAndGameMode(login, mode).stream()
            .map(ExpertGame::getImg)
            .collect(Collectors.toList());
        return images.stream()
            .filter(i -> !games.contains(i.getId()))
            .collect(Collectors.toList());
    }

    private List<Image> filterImageListByGameNumber(List<Image> images, GameMode mode) {
        if(images.isEmpty()) {
            return images;
        }
        int maxPlayed = images.stream()
            .collect(Collectors.maxBy(Comparator.comparingInt(i->i.getModeStatus().get(mode).getGameNumber())))
            .get().getModeStatus().get(mode).getGameNumber();
        return images.stream()
            .filter(i->i.getModeStatus().get(mode).getGameNumber() == maxPlayed)
            .collect(Collectors.toList());
    }

    private List<Image> filterImageListBySet(List<Image> images) {
        int minSetPriority = images.stream()
            .collect(Collectors.minBy(Comparator.comparingInt(Image::getSetPriority)))
            .get().getSetPriority();
        return images.stream()
            .filter(i -> i.getSetPriority() == minSetPriority)
            .collect(Collectors.toList());
    }

    private int compareImageNames(String n1, String n2) {
        if(n1.equals(n2)) {
            return 0;
        }

        //Images must be treated in ascending order on their suffix (_0, _1, _2, ...)
        int i=n1.length()-1;
        char c=n1.charAt(i);
        while(Character.isDigit(c) && i>=0) {
            i--;
            c=n1.charAt(i);
        }
        int suffix1 = Integer.parseInt(n1.substring(i+1));
        i=n2.length()-1;
        c=n2.charAt(i);
        while(Character.isDigit(c) && i>=0) {
            i--;
            c=n2.charAt(i);
        }
        int suffix2 = Integer.parseInt(n2.substring(i+1));
        if(suffix1 > suffix2) {
            return -1;
        }
        if(suffix2 > suffix1) {
            return 1;
        }

        //In case of suffix equality, images must be treated in ascending order on their prefix
        i=0;
        c=n1.charAt(i);
        while(Character.isDigit(c) && i<n1.length()) {
            i++;
            c=n1.charAt(i);
        }
        int prefix1 = Integer.parseInt(n1.substring(0, i));
        i=0;
        c=n2.charAt(i);
        while(Character.isDigit(c) && i<n2.length()) {
            i++;
            c=n2.charAt(i);
        }
        int prefix2 = Integer.parseInt(n2.substring(0, i));
        if(prefix1 > prefix2) {
            return -1;
        }
        if(prefix2 > prefix1) {
            return 1;
        }

        return 0;
    }

    /**
     * Finds one image among the images that have never been played by the {@code login} user on the {@code mode} game mode.
     * @param mode
     * @param login
     * @return
     */
    public Image findImage(GameMode mode, String login) {
        User user = userRepository.findOneByLogin(login).get();
        Set<Pair<String,GameMode>> skippedImages = user.getSkippedImages();

        List<String> played = gameRepository.findAllByUsrAndGameMode(login, mode).stream().map(Game::getImg).collect(Collectors.toList());
        List<String> skipped = skippedImages.stream()
            .filter(p -> p.getY().equals(mode))
            .map(p -> p.getX())
            .collect(Collectors.toList());

        List<Image> images = mongoTemplate.find(query(where("mode_status." + mode + ".status").is(ImageStatus.NOT_PROCESSED.toString())), Image.class);

        //Filter out images already played in the given game mode
        images = images.stream()
            .filter(i -> !played.contains(i.getId()))
            .collect(Collectors.toList());

        if(images.isEmpty()) {
            images = mongoTemplate.find(query(where("mode_status." + mode + ".status").is(ImageStatus.IN_PROCESSING.toString())), Image.class);
            images = images.stream()
                .filter(i -> !played.contains(i.getId()))
                .collect(Collectors.toList());
            images = images.stream()
                .sorted(Comparator.comparingInt(i->i.getModeStatus().get(mode).getGameNumber()))
                .collect(Collectors.toList());
            if(images.isEmpty()) {
                images = mongoTemplate.find(query(where("mode_status." + mode + ".status").is(ImageStatus.PROCESSED.toString())), Image.class);
                images = images.stream()
                    .filter(i -> !played.contains(i.getId()))
                    .collect(Collectors.toList());
                images.stream()
                    .forEach(i -> {
                        if (skipped.contains(i.getId())) {
                            skippedImages.removeIf(p -> p.getX().equals(i) && p.getY().equals(mode));
                        }
                    });
                if(images.isEmpty()) {
                    log.debug("No eligible image was found");
                    return null;
                }
            }
        }
        //Only images from the highest priority set
        images = filterImageListBySet(images);
        ImageSet imageSet = imageSetRepository.findByNamegit status(images.get(0).getImageSet());
        log.debug("Total number of eligible images after set filtering : {}", images.size());
        //Only non-skipped images unless only skipped images remain
        List<Image> tmp = images.stream()
            .filter(i->!skipped.contains(i.getId()))
            .collect(Collectors.toList());
        if (tmp.isEmpty()) {
            log.debug("Total number of eligible images after skipped images filtering : 0");
            List<Image> l = images;
            //Reinitializing the relevant subset of skipped images to allow users to choose which one the want to process first
            skippedImages.removeIf(p -> p.getY().equals(mode) && l.stream().anyMatch(i -> i.getId().equals(p.getX())));
        } else {
            images = tmp;
            log.debug("Total number of eligible images after skipped images filtering : {}", images.size());
        }
        //Only images with the highest ongoing game number but less than the required number of submissions
        images = images.stream().filter(i->i.getModeStatus().get(mode).getGameNumber() < imageSet.getRequiredSubmissions()).collect(Collectors.toList());
        images = filterImageListByGameNumber(images, mode);
        log.debug("Total number of eligible images after game number filtering : {}", images.size());
        //Only images with the highest game number in the other mode
        GameMode altMode;
        switch(mode) {
            case AnimalIdentification: altMode = GameMode.PlantIdentification; break;
            case PlantIdentification: altMode = GameMode.AnimalIdentification; break;
            default: altMode = null;
        }
        if(altMode != null) {
            images = filterImageListByGameNumber(images, altMode);
            log.debug("Total number of eligible images after {} filtering : {}", altMode, images.size());
        }

        log.debug("Total number of eligible images : {}", images.size());
        if(images.isEmpty()) {
            userRepository.save(user);
            return null;
        }
        //Sort images in ascending order on their name
        images.sort((i1,i2)->compareImageNames(i1.getName(),i2.getName()));
        Image image = images.get(0);
        //If the image attributed was a skipped image, remove it from the skipped images list
        skippedImages.removeIf(p -> p.getX().equals(image.getId()) && p.getY().equals(mode));
        userRepository.save(user);
        return image;
    }

    /**
     * Finds one image among the images that have never been played by the {@code login} user on the {@code mode} training game mode.
     * @param mode
     * @param login
     * @return
     */
    public Image findTrainingImage(GameMode mode, String login) {
        User user = userRepository.findOneByLogin(login).get();
        Set<Pair<String,GameMode>> skippedImages = user.getSkippedImages();
        List<String> skipped = skippedImages.stream()
            .filter(p -> p.getY().equals(mode))
            .map(p -> p.getX())
            .collect(Collectors.toList());

        //Eligible images are in the PROCESSED status for the requested mode.
        List<Image> images = mongoTemplate.find(query(where("mode_status."+mode+".status").is(ImageStatus.PROCESSED.toString())), Image.class);
        //Eligible images should not be completely empty
        images = filterEmptyImages(images, mode);
        //Eligible images have not already been played by the requesting player.
        //TODO filterPlayedImages?
        images = filterPlayedTrainingImages(images, mode, login);
        if(images.isEmpty()) {
            return null;
        }
        List<Image> tmp = images.stream()
            .filter(i->!skipped.contains(i.getId()))
            .collect(Collectors.toList());
        if (tmp.isEmpty()) {
            log.debug("Total number of eligible images after skipped images filtering : 0");
            List<Image> l = images;
            //Reinitializing the relevant subset of skipped images to allow users to choose which one they want to process first
            skippedImages.removeIf(p -> p.getY().equals(mode) && l.stream().anyMatch(i -> i.getId().equals(p.getX())));
        } else {
            images = tmp;
            log.debug("Total number of eligible images after skipped images filtering : {}", images.size());
        }
        log.debug("Total number of eligible images : {}", images.size());
        Image image = images.get(rand.nextInt(images.size()));
        skippedImages.removeIf(p -> p.getY().equals(mode) && p.getX().equals(image.getId()));
        userRepository.save(user);
        return image;
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
