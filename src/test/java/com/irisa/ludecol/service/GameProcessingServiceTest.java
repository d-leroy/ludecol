package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.subdomain.AnimalIdentificationResult;
import com.irisa.ludecol.domain.subdomain.AnimalSpecies;
import com.irisa.ludecol.domain.subdomain.GameResult;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by dorian on 02/11/15.
 */
public class GameProcessingServiceTest {


    private Random rand = new Random();
    private int nb_points = 10;
    private int nb_results = 3;

    @Test
    public void processAnimalsTest() {
        List<AnimalIdentificationResult> gameResults = new ArrayList<>();

        final List<double[]> base = new ArrayList<>();
        for(int i=0;i<nb_points;i++) {
            base.add(new double[]{rand.nextDouble() * 500,rand.nextDouble() * 500});
        }
        for(int i=0;i<nb_results;i++) {
            AnimalIdentificationResult result = new AnimalIdentificationResult();
            List<double[]> tmp = base.stream().map(a->new double[]{a[0]+(0.5-rand.nextDouble())*16,a[1]+(0.5-rand.nextDouble())*16}).collect(Collectors.toList());
            result.getSpeciesMap().put(AnimalSpecies.Mussel,tmp);
            gameResults.add(result);
        }


    }
}
