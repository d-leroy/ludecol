package com.irisa.ludecol.web.rest.dto;

import com.irisa.ludecol.domain.subdomain.AnimalSpecies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dorian on 12/05/15.
 */
public class AnimalTrainingGameDTO extends TrainingGameDTO {

    private final Map<AnimalSpecies,Integer> missingSpecies = new HashMap<>();
    private final Map<AnimalSpecies,Integer> maxSpecies = new HashMap<>();
    private final Map<AnimalSpecies,List<double[]>> partialResult = new HashMap<>();

    public Map<AnimalSpecies, Integer> getMissingSpecies() {
        return missingSpecies;
    }

    public Map<AnimalSpecies, Integer> getMaxSpecies() {
        return maxSpecies;
    }

    public Map<AnimalSpecies, List<double[]>> getPartialResult() {
        return partialResult;
    }

    @Override
    public String toString() {
        return "AnimalTrainingGameDTO{" +
            "missingSpecies=" + missingSpecies +
            ", maxSpecies=" + maxSpecies +
            ", partialResult=" + partialResult +
            '}';
    }
}
