package com.irisa.ludecol.domain.subdomain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by dorian on 04/05/15.
 */
public class AllStarsResult extends GameResult/*<Species,Boolean>*/ {

    @Field("species_map")
    @JsonProperty("species_map")
    private Map<Species,Boolean> speciesMap = new EnumMap<>(Species.class);

    /*@Override*/
    public Map<Species, Boolean> getSpeciesMap() {
        return speciesMap;
    }

    /*@Override*/
    public void setSpeciesMap(Map<Species, Boolean> speciesMap) {
        this.speciesMap = speciesMap;
    }

    @Override
    public String toString() {
        return "AllStarsResult{" +
            "speciesMap=" + speciesMap +
            '}';
    }
}
