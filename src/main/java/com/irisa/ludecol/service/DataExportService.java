package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.ReferenceGame;
import com.irisa.ludecol.repository.ReferenceGameRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.*;
import java.util.Date;
import java.util.List;

/**
 * Created by dorian on 03/07/15.
 */
@Service
public class DataExportService {

    @Inject
    private ReferenceGameRepository referenceGameRepository;

    public String export() throws IOException {
        List<ReferenceGame> referenceGames = referenceGameRepository.findAll();

        Date date = new Date();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("src/main/webapp/exported/"+date.getTime())), "utf-8"))) {
            for(ReferenceGame referenceGame : referenceGames) {
                writer.write(referenceGame.toString() + "\n");
            }
        }
        return "";

    }
}
