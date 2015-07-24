package com.irisa.ludecol.domain.subdomain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by dorian on 04/05/15.
 */
public class ProcessedAllStarsResult extends ProcessedGameResult {

    @JsonProperty("species_map")
    @Field("species_map")
    private Map<Species,Pair<Double>> speciesMap = new EnumMap<>(Species.class);

    public Map<Species,Pair<Double>> getSpeciesMap() {
        return speciesMap;
    }

    public void setSpeciesMap(Map<Species,Pair<Double>> speciesMap) {
        this.speciesMap = speciesMap;
    }

    @Override
    public String toString() {
        return "ProcessedAllStarsResult{" +
            "speciesMap=" + speciesMap +
            '}';
    }
}
