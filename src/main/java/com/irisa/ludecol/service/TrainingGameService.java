package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.TrainingGame;
import com.irisa.ludecol.domain.subdomain.*;
import com.irisa.ludecol.repository.TrainingGameRepository;
import com.irisa.ludecol.web.rest.dto.AnimalTrainingGameDTO;
import com.irisa.ludecol.web.rest.dto.PlantTrainingGameDTO;
import com.irisa.ludecol.web.rest.dto.TrainingGameDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dorian on 07/05/15.
 */
@Service
public class TrainingGameService {

    private final Logger log = LoggerFactory.getLogger(TrainingGameService.class);

    private final int MAX_DISTANCE = 64;

    @Inject
    private TrainingGameRepository trainingGameRepository;

//    @Inject
//    private ReferenceGameRepository referenceGameRepository;

    @Inject
    private ImageProviderService imageProviderService;

    private List<double[]> matchPointSets(final List<double[]> a, final List<double[]> b) {
        List<double[]> matchedPoints = new ArrayList<>();
        if(a != null && b != null && !a.isEmpty() && !b.isEmpty()) {
            List<double[]> referencePoints = new ArrayList<>(b);
            for (double[] p : a) {
                double d = MAX_DISTANCE * MAX_DISTANCE;
                double[] r = null;
                for (double[] q : referencePoints) {
                    Double dist = Math.pow(p[0] - q[0], 2) + Math.pow(p[1] - q[1], 2);
                    if (dist < d) {
                        d = dist;
                        r = q;
                    }
                }
                if (r != null) {
                    matchedPoints.add(new double[]{p[0],p[1]});
                    referencePoints.remove(r);
                }
            }
        }
        return matchedPoints;
    }

    private List<Boolean> correctPresenceGrid(final List<Boolean> submittedGrid, final List<Boolean> referenceGrid) {
        List<Boolean> result = new ArrayList<>();
        for (int i = 0; i < submittedGrid.size(); i++) {
            result.add(submittedGrid.get(i) && referenceGrid.get(i));
        }
        return result;
    }

    public void updateTrainingGame(final TrainingGame trainingGame, final Game game) {
        int errors = 0;
        switch (game.getGameMode()) {
            case AnimalIdentification: {
                Map<AnimalSpecies,List<double[]>> submittedMap = ((AnimalIdentificationResult) game.getGameResult()).getSpeciesMap();
                Map<AnimalSpecies,List<double[]>> referenceMap = ((AnimalIdentificationResult) trainingGame.getReferenceResult()).getSpeciesMap();
                Map<AnimalSpecies,List<double[]>> correctedMap = new HashMap();

                for(AnimalSpecies key : submittedMap.keySet()) {
                    List<double[]> referenceList = referenceMap.get(key);
                    List<double[]> correctedList = matchPointSets(submittedMap.get(key),referenceList);
                    int missing = referenceList.size() - correctedList.size();
                    correctedMap.put(key, correctedList);
                    errors += missing;
                }
                AnimalIdentificationResult animalIdentificationResult = new AnimalIdentificationResult();
                animalIdentificationResult.setSpeciesMap(correctedMap);
                trainingGame.setSubmittedResult(animalIdentificationResult);
            }
            break;
            case PlantIdentification: {
                Map<PlantSpecies,List<Boolean>> submittedMap = ((PlantIdentificationResult) game.getGameResult()).getSpeciesMap();
                Map<PlantSpecies,List<Boolean>> referenceMap = ((PlantIdentificationResult) trainingGame.getReferenceResult()).getSpeciesMap();
                Map<PlantSpecies,List<Boolean>> correctedMap = new HashMap();

                for(PlantSpecies key : submittedMap.keySet()) {
                    List<Boolean> referenceList = referenceMap.get(key);
                    List<Boolean> correctedList = correctPresenceGrid(submittedMap.get(key),referenceList);

                    int max = 0;
                    for(boolean b : referenceList) {max = b ? max+1 : max;}

                    int current = 0;
                    for(boolean b : correctedList) {current = b ? current+1 : current;}

                    int missing = max - current;
                    correctedMap.put(key,correctedList);
                    errors += missing;
                }
                PlantIdentificationResult plantIdentificationResult = new PlantIdentificationResult();
                plantIdentificationResult.setSpeciesMap(correctedMap);
                trainingGame.setSubmittedResult(plantIdentificationResult);
            }
            break;
            default:
                return;
        }

        if (errors == 0) {trainingGame.setCompleted(true);}
        else {trainingGame.setScore(Math.max(trainingGame.getScore() - 5 * errors, 50));}

        trainingGameRepository.save(trainingGame);
    }

    public TrainingGameDTO getTrainingGameWrapper(final TrainingGame trainingGame) {
        TrainingGameDTO result = null;
        if(trainingGame != null)
            switch(trainingGame.getGameMode()) {
                case AnimalIdentification: {
                    AnimalTrainingGameDTO wrapper = new AnimalTrainingGameDTO();

                    Map<AnimalSpecies,List<double[]>> submittedMap = ((AnimalIdentificationResult) trainingGame.getSubmittedResult()).getSpeciesMap();
                    Map<AnimalSpecies,List<double[]>> referenceMap = ((AnimalIdentificationResult) trainingGame.getReferenceResult()).getSpeciesMap();
                    for(Object key : referenceMap.keySet()) {
                        List<double[]> referenceList = null;
                        List<double[]> submittedList = null;
//                        if(key instanceof String) {
//                            AnimalSpecies enumKey = AnimalSpecies.valueOf((String)key);
//                            referenceList = referenceMap.get(enumKey);
//                            submittedList = submittedMap.get(enumKey);
//                            int max = referenceList.size();
//                            wrapper.getPartialResult().put(enumKey, submittedList);
//                            wrapper.getMissingSpecies().put(enumKey, max - submittedList.size());
//                            wrapper.getMaxSpecies().put(enumKey, max);
//                        } else if(key instanceof AnimalSpecies) {
                            referenceList = referenceMap.get(key);
                            submittedList = submittedMap.get(key);
                            int max = referenceList.size();
                            wrapper.getPartialResult().put((AnimalSpecies) key, submittedList);
                            wrapper.getMissingSpecies().put((AnimalSpecies) key, max - submittedList.size());
                            wrapper.getMaxSpecies().put((AnimalSpecies) key, max);
//                        }
                    }

                    result = wrapper;
                }
                break;
                case PlantIdentification: {
                    PlantTrainingGameDTO wrapper = new PlantTrainingGameDTO();

                    Map<PlantSpecies,List<Boolean>> submittedMap = ((PlantIdentificationResult) trainingGame.getSubmittedResult()).getSpeciesMap();
                    Map<PlantSpecies,List<Boolean>> referenceMap = ((PlantIdentificationResult) trainingGame.getReferenceResult()).getSpeciesMap();

                    for(PlantSpecies key : referenceMap.keySet()) {
                        List<Boolean> referenceList = referenceMap.get(key);
                        List<Boolean> submittedList = submittedMap.get(key);

                        int max = 0;
                        for(boolean b : referenceList) {max = b ? max+1 : max;}

                        int current = 0;
                        for(boolean b : submittedList) {current = b ? current+1 : current;}

                        wrapper.getPartialResult().put(key,submittedList);
                        wrapper.getMissingSpecies().put(key, max - current);
                        wrapper.getMaxSpecies().put(key,max);
                    }

                    result = wrapper;
                }
                break;
                default:
                    return null;
            }
        result.setId(trainingGame.getId());
        result.setImg(trainingGame.getImg());
        result.setScore(trainingGame.getScore());
        result.setCompleted(trainingGame.getCompleted());
        return result;
    }

    public TrainingGameDTO createTrainingGame(Game game) {
        GameMode mode = game.getGameMode();
        String login = game.getUsr();
        Image img = imageProviderService.findTrainingImage(mode, login);
        if(img == null)
            return null;
        ImageModeStatus modeStatus = img.getModeStatus().get(mode);
        if(modeStatus == null)
            return null;
        TrainingGame trainingGame;
        GameResult refResult;
        switch(mode) {
            case AnimalIdentification: {
                refResult = new AnimalIdentificationResult();
                Map<AnimalSpecies,List<double[]>> refMap = new HashMap();
                Map tmpMap = modeStatus.getReferenceResult();
                for(Object key : tmpMap.keySet()) {
                    if(key instanceof String) {
                        refMap.put(AnimalSpecies.valueOf((String)key),(List<double[]>)tmpMap.get(key));
                    } else if(key instanceof AnimalSpecies){
                        refMap.put((AnimalSpecies)key,(List<double[]>)tmpMap.get(key));
                    }
                }
                ((AnimalIdentificationResult) refResult).setSpeciesMap(refMap);
                if(refResult == null) {
                    return null;
                }
                trainingGame = new TrainingGame<AnimalIdentificationResult>();
            }
            break;
            case PlantIdentification: {
                refResult = new PlantIdentificationResult();
                Map<PlantSpecies,List<Boolean>> refMap = new HashMap();
                Map tmpMap = modeStatus.getReferenceResult();
                for(Object key : tmpMap.keySet()) {
                    if(key instanceof String) {
                        refMap.put(PlantSpecies.valueOf((String)key),(List<Boolean>)tmpMap.get(key));
                    } else if(key instanceof PlantSpecies){
                        refMap.put((PlantSpecies)key,(List<Boolean>)tmpMap.get(key));
                    }
                }
                ((PlantIdentificationResult) refResult).setSpeciesMap(refMap);
                if(refResult == null) {
                    return null;
                }
                trainingGame = new TrainingGame<PlantIdentificationResult>();
            }
            break;
            default:
                return null;
        }
        trainingGame.setCompleted(false);
        trainingGame.setScore(100);
        trainingGame.setUsr(login);
        trainingGame.setImg(img.getId());
        trainingGame.setGameMode(mode);
        trainingGame.setSubmittedResult(game.getGameResult());
        trainingGame.setReferenceResult(refResult);
        trainingGameRepository.save(trainingGame);
        return getTrainingGameWrapper(trainingGame);
    }
}
