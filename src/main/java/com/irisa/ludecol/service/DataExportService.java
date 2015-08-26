package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.ReferenceGame;
import com.irisa.ludecol.repository.ImageRepository;
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

    @Inject
    private ImageRepository imageRepository;

    public String export() throws IOException {
        List<ReferenceGame> referenceGames = referenceGameRepository.findAll();

        Date date = new Date();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("src/main/webapp/exported/"+date.getTime())), "utf-8"))) {
            for(ReferenceGame referenceGame : referenceGames) {
                writer.write("Image : " + imageRepository.findOne(referenceGame.getImg()).getName() + "\n");
                writer.write("  Mode : " + referenceGame.getGameMode().toString() + "\n");
                writer.write("  Result : " + referenceGame.getGameResult().toString() + "\n");
                writer.write("============" + "\n");
            }
        }
        return "";

    }
}
