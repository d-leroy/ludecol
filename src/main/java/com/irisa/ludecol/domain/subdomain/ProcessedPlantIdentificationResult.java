package com.irisa.ludecol.domain.subdomain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dorian on 04/05/15.
 */
public class ProcessedPlantIdentificationResult extends ProcessedGameResult {

    @JsonProperty("species_map")
    @Field("species_map")
    private Map<PlantSpecies,List<Double>> speciesMap = new EnumMap<>(PlantSpecies.class);

    public Map<PlantSpecies, List<Double>> getSpeciesMap() {
        return speciesMap;
    }

    public void setSpeciesMap(Map<PlantSpecies, List<Double>> speciesMap) {
        this.speciesMap = speciesMap;
    }

    @Override
    public String toString() {
        return "ProcessedPlantIdentificationResult{" +
            "speciesMap=" + speciesMap +
            '}';
    }
}
