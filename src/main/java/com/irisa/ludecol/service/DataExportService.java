package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.repository.ImageRepository;
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
    private ImageRepository imageRepository;

    public String exportAll() throws IOException {

        List<Image> images = imageRepository.findAll();

        Date date = new Date();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("src/main/webapp/exported/"+date.getTime())), "utf-8"))) {
            for(Image image : images) {
                writer.write("Image : " + image.getName() + ";\n");
                image.getModeStatus().forEach((mode,status) -> {
                    try {
                        writer.write("  Mode : " + mode.toString() + ";\n");
                        writer.write("  Result : " + status.getReferenceResult().toString() + ";\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                writer.write("============" + "\n");
            }
        }
        return "";
    }
}
