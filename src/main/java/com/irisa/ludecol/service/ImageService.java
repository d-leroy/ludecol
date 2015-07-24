package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.repository.ImageRepository;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by dorian on 10/07/15.
 */
@Service
public class ImageService {

    @Inject
    private ImageRepository imageRepository;

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    public void insertImage(File file, String imageSetName) {
        try {
            BufferedImage img = ImageIO.read(file);
            String[] tokens = file.getName().split("\\.(?=[^\\.]+$)");
            Path filePath = Paths.get("src/main/webapp/tiles/" + imageSetName + "/" + tokens[0]);
            if (!Files.isRegularFile(filePath)) {
                String name = filePath.getFileName().toString();
                String path = "/tiles/" + imageSetName + "/" + name + "/";
                Image image = imageRepository.findOneByPath(path);
                if (image == null) {
                    image = new Image();
                    image.setName(name);
                    image.setPath(path);
                    image.setWidth(img.getWidth());
                    image.setHeight(img.getHeight());
                    image.setGameModes(EnumSet.of(GameMode.AllStars));
                    image.setImageSet(imageSetName);
                    imageRepository.save(image);
                    log.debug("Saved Image : {}", img);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void zoomifyImage(File file, String imageSetName) {
        String imageName = file.getName();
        try {
            File image = Paths.get("src/main/webapp/tiles/"+imageSetName+"/"+imageName).toFile();
            File dir = Paths.get("src/main/webapp/tiles/" + imageSetName).toFile();
            dir.mkdir();
            OutputStream outputStream = new FileOutputStream(image);
            Files.copy(file.toPath(), outputStream);
            outputStream.flush();
            outputStream.close();

            ProcessBuilder pb = new ProcessBuilder("python", "../../../../../ZoomifyImage/ZoomifyFileProcessor.py", imageName);
            pb.directory(dir);

            Process p = pb.start();
            p.waitFor();
            image.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void thumbnailize(File file, String imageSetName) {
        try {
            File dir = Paths.get("src/main/webapp/images/"+imageSetName+"/thumbnail").toFile();
            dir.mkdirs();
            Thumbnails.of(file)
                .size(100,100)
                .outputFormat("JPG")
                .toFiles(dir, Rename.PREFIX_DOT_THUMBNAIL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<File> splitImage(int cols, int rows, String imageSetName, File file) {
        List<File> result = new ArrayList<>();
        if(file.isFile()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedImage image = ImageIO.read(fis);
                int chunks = rows * cols;

                int chunkWidth = image.getWidth() / cols; // determines the chunk width and height
                int chunkHeight = image.getHeight() / rows;
                int count = 0;
                BufferedImage imgs[] = new BufferedImage[chunks]; //Image array to hold image chunks
                for (int x = 0; x < rows; x++) {
                    for (int y = 0; y < cols; y++) {
                        //Initialize the image array with image chunks
                        imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());
                        // draws the image chunk
                        Graphics2D gr = imgs[count++].createGraphics();
                        gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                        gr.dispose();
                    }
                }

                Paths.get("src/main/webapp/images/" + imageSetName + "/divided").toFile().mkdir();

                for (int i = 0; i < imgs.length; i++) {
                    String[] tokens = file.getName().split("\\.(?=[^\\.]+$)"); //splits the filename into basename and extension.
                    File chunkImg = new File("src/main/webapp/images/" + imageSetName + "/divided/" + tokens[0] + "_" + i + "." + tokens[1]);
                    boolean success = ImageIO.write(imgs[i], tokens[1], chunkImg);
                    if(success) {
                        result.add(chunkImg);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
