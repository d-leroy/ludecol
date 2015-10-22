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
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
        int maxPlayed = images.get(0).getModeStatus().get(mode).getGameNumber();
        return images.stream()
            .filter(i->i.getModeStatus().get(mode).getGameNumber() == maxPlayed)
            .collect(Collectors.toList());
    }

    private List<Image> filterImageListBySet(List<Image> images) {
        ImageSet imageSet = imageSetRepository.findAll().stream()
            .collect(Collectors.minBy(Comparator.comparingInt(ImageSet::getPriority))).get();
        return images.stream()
            .filter(i->imageSet.getName().equals(i.getImageSet()))
            .collect(Collectors.toList());
//        int maxSetPriority = images.stream()
//            .collect(Collectors.minBy(Comparator.comparingInt(Image::getSetPriority)))
//            .get().getSetPriority();
//        return images.stream()
//            .filter(i -> i.getSetPriority() == maxSetPriority)
//            .collect(Collectors.toList());
    }

    private int compareImageNames(String n1, String n2) {
        if(n1.equals(n2)) {
            return 0;
        }

        //Images must be treated by ascending order on their suffix (_0, _1, _2, ...)
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

        //In case of suffix equality, images must be treated by ascending order on their prefix
        i=0;
        c=n1.charAt(i);
        while(Character.isDigit(c) && i<n1.length()) {
            i++;
            c=n1.charAt(i);
        }
        int prefix1 = Integer.parseInt(n1.substring(0, i-1));
        i=0;
        c=n2.charAt(i);
        while(Character.isDigit(c) && i<n2.length()) {
            i++;
            c=n2.charAt(i);
        }
        int prefix2 = Integer.parseInt(n1.substring(0, i-1));
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

        images = images.stream()
            .filter(i -> !played.contains(i.getId()))
            .collect(Collectors.toList());

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
        //Only images with the highest game number
        images = filterImageListByGameNumber(images, mode);
        log.debug("Total number of eligible images : {}", images.size());
        if(images.isEmpty()) {
            userRepository.save(user);
            return null;
        }
        //Only non-skipped images unless only skipped images remain
        List<Image> tmp = images.stream()
            .filter(i->!skipped.contains(i.getId()))
            .collect(Collectors.toList());
        if(!tmp.isEmpty()) {
            images = tmp;
        }
//        Image image = images.get(rand.nextInt(images.size()));
        images.sort((i1,i2)->compareImageNames(i1.getName(),i2.getName()));
        Image image = images.get(0);
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
        //Eligible images have not already been played by the requesting player.
        images = filterPlayedTrainingImages(images, mode, login);
        images = images.stream()
            .sorted(Comparator.comparingInt(i -> i.getModeStatus().get(mode).getGameNumber()))
            .collect(Collectors.toList());
        if(images.isEmpty()) {
            return null;
        }
            List<Image> tmp = images.stream()
            .filter(i->!skipped.contains(i.getId()))
            .collect(Collectors.toList());
        if(!tmp.isEmpty()) {
            images = tmp;
        }
        log.debug("Total number of eligible images : {}", images.size());
        Image image = images.get(rand.nextInt(images.size()));
        skippedImages.removeIf(p -> p.getX().equals(image.getId()) && p.getY().equals(mode));
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
