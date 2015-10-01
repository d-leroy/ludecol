package com.irisa.ludecol.domain.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.subdomain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

//import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

/**
 * Created by dorian on 05/05/15.
 */
public class CustomGameDeserializer extends JsonDeserializer<Game> {

    private final Logger log = LoggerFactory.getLogger(CustomGameDeserializer.class);

    @Override
    public Game deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, double[].class);
        JavaType mapType = tf.constructMapType(HashMap.class, Species.class, Integer.class);
        Game result = new Game();
        if (node.get("usr") != null)
            result.setUser(node.get("usr").asText());
        if (node.get("img") != null)
            result.setImg(node.get("img").asText());
        if (node.get("id") != null)
            result.setId(node.get("id").asText());
        if (node.get("completed") != null)
            result.setCompleted(node.get("completed").asBoolean());
        if (node.get("game_mode") != null) {
            GameMode mode = GameMode.valueOf(node.get("game_mode").asText());
            result.setGameMode(mode);

            switch (mode) {
                case AllStars: {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonParser map = node.get("game_result").traverse();
                    HashMap<Species,Boolean> tmp = mapper.readValue(map,mapType);
                    AllStarsResult res = new AllStarsResult();
                    res.setSpeciesMap(tmp);
                    result.setGameResult(res);
                    break;
                }
                case AnimalIdentification: {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonParser snailList = node.get("game_result").get("Snail").traverse();
                    List<double[]> tmp = mapper.readValue(snailList, listType);
                    AnimalIdentificationResult animalIdentificationResult = new AnimalIdentificationResult();
                    animalIdentificationResult.getSpeciesMap().put(AnimalSpecies.Snail,tmp);
                    JsonParser musselList = node.get("game_result").get("Mussel").traverse();
                    tmp = mapper.readValue(musselList, listType);
                    animalIdentificationResult.getSpeciesMap().put(AnimalSpecies.Mussel, tmp);
                    JsonParser crabList = node.get("game_result").get("Crab").traverse();
                    tmp = mapper.readValue(crabList, listType);
                    animalIdentificationResult.getSpeciesMap().put(AnimalSpecies.Crab, tmp);
                    JsonParser burrowList = node.get("game_result").get("Burrow").traverse();
                    tmp = mapper.readValue(burrowList, listType);
                    animalIdentificationResult.getSpeciesMap().put(AnimalSpecies.Burrow, tmp);
                    result.setGameResult(animalIdentificationResult);
                    break;
                }
                case PlantIdentification: {
                    ObjectMapper mapper = new ObjectMapper();
                    PlantIdentificationResult plantIdentificationResult = new PlantIdentificationResult();
                    JsonParser batisList = node.get("game_result").get("Batis").traverse();
                    List<Boolean> tmp = mapper.readValue(batisList, List.class);
                    plantIdentificationResult.getSpeciesMap().put(PlantSpecies.Batis, tmp);
                    JsonParser spartinaList = node.get("game_result").get("Spartina").traverse();
                    tmp = mapper.readValue(spartinaList, List.class);
                    plantIdentificationResult.getSpeciesMap().put(PlantSpecies.Spartina, tmp);
                    JsonParser salicorniaList = node.get("game_result").get("Salicornia").traverse();
                    tmp = mapper.readValue(salicorniaList, List.class);
                    plantIdentificationResult.getSpeciesMap().put(PlantSpecies.Salicornia, tmp);
                    JsonParser borrichiaList = node.get("game_result").get("Borrichia").traverse();
                    tmp = mapper.readValue(borrichiaList, List.class);
                    plantIdentificationResult.getSpeciesMap().put(PlantSpecies.Borrichia, tmp);
                    JsonParser limoniumList = node.get("game_result").get("Limonium").traverse();
                    tmp = mapper.readValue(limoniumList, List.class);
                    plantIdentificationResult.getSpeciesMap().put(PlantSpecies.Limonium, tmp);
                    JsonParser juncusList = node.get("game_result").get("Juncus").traverse();
                    tmp = mapper.readValue(juncusList, List.class);
                    plantIdentificationResult.getSpeciesMap().put(PlantSpecies.Juncus, tmp);
                    result.setGameResult(plantIdentificationResult);
                    break;
                }
                default:
            }
        }
        return result;
    }
}
