package com.irisa.ludecol.domain.subdomain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dorian on 04/05/15.
 */
public class PlantIdentificationResult extends GameResult/*<PlantSpecies,List<Boolean>>*/ {

    @Field("species_map")
    @JsonProperty("species_map")
    private Map<PlantSpecies,List<Boolean>> speciesMap = new HashMap();

    /*@Override*/
    public Map<PlantSpecies, List<Boolean>> getSpeciesMap() {
        return speciesMap;
    }

    /*@Override*/
    public void setSpeciesMap(Map<PlantSpecies, List<Boolean>> speciesMap) {
        this.speciesMap = speciesMap;
    }

    @Override
    public String toString() {
        return "PlantIdentificationResult{" +
            "speciesMap=" + speciesMap +
            '}';
    }
}
