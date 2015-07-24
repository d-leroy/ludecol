package com.irisa.ludecol.domain.subdomain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dorian on 05/05/15.
 */
@JsonAutoDetect
public class ProcessedAnimalIdentificationResult extends ProcessedGameResult {

    @JsonProperty("species_map")
    @Field("species_map")
    private Map<AnimalSpecies,List<double[]>> speciesMap = new EnumMap<>(AnimalSpecies.class);

    public Map<AnimalSpecies, List<double[]>> getSpeciesMap() {
        return speciesMap;
    }

    public void setSpeciesMap(Map<AnimalSpecies, List<double[]>> speciesMap) {
        this.speciesMap = speciesMap;
    }

    @Override
    public String toString() {
        return "ProcessedAnimalIdentificationResult{" +
            "speciesMap=" + speciesMap +
            '}';
    }
}
