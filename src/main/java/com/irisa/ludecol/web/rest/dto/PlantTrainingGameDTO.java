package com.irisa.ludecol.web.rest.dto;

import com.irisa.ludecol.domain.subdomain.PlantSpecies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dorian on 12/05/15.
 */
public class PlantTrainingGameDTO extends TrainingGameDTO {

    private final Map<PlantSpecies,Integer> missingSpecies = new HashMap<>();
    private final Map<PlantSpecies,Integer> maxSpecies = new HashMap<>();
    private final Map<PlantSpecies,List<Boolean>> partialResult = new HashMap<>();

    public Map<PlantSpecies,Integer> getMissingSpecies() {
        return missingSpecies;
    }

    public Map<PlantSpecies,Integer> getMaxSpecies() {
        return maxSpecies;
    }

    public Map<PlantSpecies,List<Boolean>> getPartialResult() {
        return partialResult;
    }

    @Override
    public String toString() {
        return "PlantTrainingGameDTO{" +
            "missingSpecies=" + missingSpecies +
            ", maxSpecies=" + maxSpecies +
            ", partialResult=" + partialResult +
            '}';
    }
}
