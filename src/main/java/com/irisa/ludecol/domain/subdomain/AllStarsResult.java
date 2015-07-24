package com.irisa.ludecol.domain.subdomain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by dorian on 04/05/15.
 */
public class AllStarsResult  extends GameResult {

    @Field("species_map")
    @JsonProperty("species_map")
    private Map<Species,Integer> speciesMap = new EnumMap<>(Species.class);

    public Map<Species, Integer> getSpeciesMap() {
        return speciesMap;
    }

    public void setSpeciesMap(Map<Species, Integer> speciesMap) {
        this.speciesMap = speciesMap;
    }

    @Override
    public String toString() {
        return "AllStarsResult{" +
            "speciesMap=" + speciesMap +
            '}';
    }
}
