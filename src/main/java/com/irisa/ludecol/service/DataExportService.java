package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.ImageSet;
import com.irisa.ludecol.domain.subdomain.AnimalSpecies;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageModeStatus;
import com.irisa.ludecol.domain.subdomain.PlantSpecies;
import com.irisa.ludecol.repository.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dorian on 03/07/15.
 */
@Service
public class DataExportService {

    private final Logger log = LoggerFactory.getLogger(DataExportService.class);

    @Inject
    private ImageRepository imageRepository;

    public static class DataWrapper {
        public String result;

        @Override
        public String toString() {
            return "DataWrapper{" +
                "result='" + result + '\'' +
                '}';
        }
    }

    public DataWrapper exportImageStatistics() {

        List<Image> images = imageRepository.findAll();

        StringBuilder builder = new StringBuilder();

        builder.append("Image" + "\t" + "Mode" + "\t" + "Number of submissions" + "\n");

        for (Image image : images) {
            Map<GameMode,ImageModeStatus> map = image.getModeStatus();
            builder.append(image.getName() + "\t" + "Animal Identification" + "\t" + map.get(GameMode.AnimalIdentification).getGameNumber() + "\n");
        }

        for (Image image : images) {
            Map<GameMode,ImageModeStatus> map = image.getModeStatus();
            builder.append(image.getName() + "\t" + "Plant Identification" + "\t" + map.get(GameMode.PlantIdentification).getGameNumber() + "\n");
        }

        DataWrapper result = new DataWrapper();

        result.result = builder.toString();

        return result;
    }

    public DataWrapper exportSet(ImageSet imageSet) {

        List<Image> images = imageRepository.findByImageSet(imageSet.getName());

        StringBuilder builder = new StringBuilder();

        Map<AnimalSpecies,Integer> animalMap = new HashMap<>();
        Map<PlantSpecies,Double> plantMap = new HashMap<>();
        builder.append("Image" + "\t" + "Burrow" + "\t" + "Crab" + "\t" + "Mussel" + "\t" + "Snail" + "\t"
            + "Batis" + "\t" + "Borrichia" + "\t" + "Juncus" + "\t" + "Limonium" + "\t" + "Salicornia" + "\t" + "Spartina" + "\n");
        for(Image image : images) {
            animalMap.clear();
            plantMap.clear();
            Map<AnimalSpecies,List<double[]>> refAnimalMap = image.getModeStatus().get(GameMode.AnimalIdentification).getReferenceResult();
            Map<PlantSpecies,List<Boolean>> refPlantMap = image.getModeStatus().get(GameMode.PlantIdentification).getReferenceResult();
            if(refAnimalMap != null && refPlantMap != null) {
                Arrays.asList(AnimalSpecies.values()).stream()
                    .forEach(s -> {
                        List<double[]> l = refAnimalMap.get(s.toString());
                        animalMap.put(s, l == null ? 0 : l.size());
                    });
                Arrays.asList(PlantSpecies.values()).stream()
                    .forEach(s -> {
                        List<Boolean> l = refPlantMap.get(s.toString());
                        int s1 = l.stream().filter(b -> b).collect(Collectors.toList()).size();
                        int s2 = l.size();
                        plantMap.put(s, l == null ? 0. : (s1 * 1.) / (s2 * 1.));
                    });
                builder.append(image.getName() + "\t"
                    + animalMap.get(AnimalSpecies.Burrow) + "\t"
                    + animalMap.get(AnimalSpecies.Crab) + "\t"
                    + animalMap.get(AnimalSpecies.Mussel) + "\t"
                    + animalMap.get(AnimalSpecies.Snail) + "\t"
                    + plantMap.get(PlantSpecies.Batis) + "\t"
                    + plantMap.get(PlantSpecies.Borrichia) + "\t"
                    + plantMap.get(PlantSpecies.Juncus) + "\t"
                    + plantMap.get(PlantSpecies.Limonium) + "\t"
                    + plantMap.get(PlantSpecies.Salicornia) + "\t"
                    + plantMap.get(PlantSpecies.Spartina) + "\t"
                    + "\n");
            }
        }
        DataWrapper result = new DataWrapper();
        result.result = builder.toString();
        return result;
    }

    public String exportAll() throws IOException {

        List<Image> images = imageRepository.findAll();

        Date date = new Date();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("src/main/webapp/exported/"+date.getTime())), "utf-8"))) {
            Map<AnimalSpecies,Integer> animalMap = new HashMap<>();
            Map<PlantSpecies,Double> plantMap = new HashMap<>();
            for(Image image : images) {
                animalMap.clear();
                plantMap.clear();
                Map<AnimalSpecies,List<double[]>> refAnimalMap = image.getModeStatus().get(GameMode.AnimalIdentification).getReferenceResult();
                Arrays.asList(AnimalSpecies.values()).stream()
                    .forEach(s->{
                        List<double[]> l = refAnimalMap.get(s);
                        animalMap.put(s, l == null ? 0 : l.size());
                    });
                Map<PlantSpecies,List<Boolean>> refPlantMap = image.getModeStatus().get(GameMode.PlantIdentification).getReferenceResult();
                Arrays.asList(PlantSpecies.values()).stream()
                    .forEach(s -> {
                        List<Boolean> l = refPlantMap.get(s);
                        plantMap.put(s, l == null ? 0. : l.stream().filter(b -> b).collect(Collectors.toList()).size() / l.size());
                    });
                writer.write(image.getName() + "\t"
                        + refAnimalMap.get(AnimalSpecies.Burrow).size() + "\t"
                        + refAnimalMap.get(AnimalSpecies.Crab).size() + "\t"
                        + refAnimalMap.get(AnimalSpecies.Mussel).size() + "\t"
                        + refAnimalMap.get(AnimalSpecies.Snail).size() + "\t"
                        + plantMap.get(PlantSpecies.Batis) + "\t"
                        + plantMap.get(PlantSpecies.Borrichia) + "\t"
                        + plantMap.get(PlantSpecies.Juncus) + "\t"
                        + plantMap.get(PlantSpecies.Limonium) + "\t"
                        + plantMap.get(PlantSpecies.Salicornia) + "\t"
                        + plantMap.get(PlantSpecies.Spartina) + "\t"
                        + "\n");
            }
        }
        return "";
    }
}
