package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.ImageSet;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageModeStatus;
import com.irisa.ludecol.domain.subdomain.ImageStatus;
import com.irisa.ludecol.repository.ExpertGameRepository;
import com.irisa.ludecol.repository.GameRepository;
import com.irisa.ludecol.repository.ImageRepository;
import com.irisa.ludecol.repository.TrainingGameRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

/**
 * Created by dorian on 10/07/15.
 */
@Service
public class ImageService {

    @Inject
    private ImageRepository imageRepository;

    @Inject
    private GameRepository gameRepository;

    @Inject
    private TrainingGameRepository trainingGameRepository;

    @Inject
    private ExpertGameRepository expertGameRepository;

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    public void insertImage(File file, ImageSet imageSet) {
        try {
            BufferedImage img = ImageIO.read(file);
            String[] tokens = file.getName().split("\\.(?=[^\\.]+$)");
            Path filePath = Paths.get("src/main/webapp/tiles/" + imageSet.getName() + "/" + tokens[0]);
            if (!Files.isRegularFile(filePath)) {
                String name = filePath.getFileName().toString();
                String path = "/tiles/" + imageSet.getName() + "/" + name + "/";
                Image image = imageRepository.findOneByPath(path);
                if (image == null) {
                    image = new Image();
                    image.setName(name);
                    image.setPath(path);
                    image.setWidth(img.getWidth());
                    image.setHeight(img.getHeight());
                    EnumMap<GameMode,ImageModeStatus> map = image.getModeStatus();
                    Arrays.asList(GameMode.values()).stream().forEach(v -> {
                        if (!v.equals(GameMode.AllStars))
                            map.put(v,new ImageModeStatus());
                        else
                            map.put(v,new ImageModeStatus(ImageStatus.NOT_PROCESSED, 0));
                    });
                    image.setImageSet(imageSet.getName());
                    image.setSetPriority(imageSet.getPriority());
                    imageRepository.save(image);
                    log.debug("Saved Image : {}", image);
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

    public void cleanupImage(String id) {
        Image img = imageRepository.findOne(id);
        cleanupImage(img);
    }

    public void cleanupImage(Image img) {
        String id = img.getId();
        File thb = Paths.get("src/main/webapp/images/"+img.getImageSet()+"/thumbnail/thumbnail."+img.getName()+".JPG").toFile();
        if(thb.delete()) {
            log.debug("Deleted thumbnail for image : {}", img.getName());
        } else {
            log.debug("Deletion of thumbnail for image {} failed", img.getName());
        }

        File dir = Paths.get("src/main/webapp/tiles/"+img.getImageSet()+"/"+img.getName()).toFile();
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.isDirectory()) {
                File[] tiles = file.listFiles();
                for (int j = 0; j < tiles.length; j++) {
                    tiles[j].delete();
                }
            }
            file.delete();
        }
        dir.delete();

        gameRepository.findAllByImg(id).stream().forEach(g -> {
            if (g.getCompleted()) g.setImg("-1");
            else gameRepository.delete(g);
        });
        trainingGameRepository.findAllByImg(id).stream().forEach(trainingGameRepository::delete);
        expertGameRepository.findAllByImg(id).stream().forEach(expertGameRepository::delete);
        imageRepository.delete(id);
    }



//    private List<ImageStatisticsDTO.GameModeStatistics> getImageModeStatistics(Image image) {
//        List<ImageStatisticsDTO.GameModeStatistics> result = new ArrayList<>();
//        Arrays.asList(GameMode.values()).stream().forEach(m->
//            image.getModeStatus().entrySet().stream().forEach(e-> {
//                ImageStatus status = e.getKey();
//                e.getValue().stream().forEach(s->
//                    result.add(new ImageStatisticsDTO.GameModeStatistics(s.getMode(), s.getGameNumber(), status)));
//            }));
//        return result;
//    }
//
//    public ImageStatisticsDTO getImageStatistics(Image image) {
//        ImageStatisticsDTO result = new ImageStatisticsDTO();
//        result.setGameModeStatistics(getImageModeStatistics(image));
//        return result;
//    }

//    public ImageSetStatisticsDTO getImageSetStatistics(String set) {
//
//        ImageSetStatisticsDTO result = new ImageSetStatisticsDTO();
//        List<ImageSetStatisticsDTO.GameModeStatistics> gameModeStatisticses = new ArrayList<>();
//
//        //how much images are in each status?
//        List<Image> images = imageRepository.findByImageSet(set);
//
//        images.stream()
//            .forEach(i->{
//                i.getModeStatus().entrySet().stream()
//                    .forEach(e->{
//                    });
//            });
//
//        List<String> images = imageRepository.findByImageSet(set).stream().map(i -> i.getId()).collect(Collectors.toList());
//
////        List<Game> games = gameRepository.findAll().stream().filter(g -> images.contains(g.getImg())).collect(Collectors.toList());
////        List<ReferenceGame> refGames = referenceGameRepository.findAll().stream().filter(g -> images.contains(g.getImg())).collect(Collectors.toList());
//        Map<String,Integer> imageMap = new HashMap<>();
//        EnumMap<ImageStatus,Integer> map = new EnumMap<>(ImageStatus.class);
//
//        refGames.stream()
//            .filter(g -> g.getGameMode().equals(GameMode.TrainingAnimalIdentification))
//            .map(g -> g.getImg())
//            .forEach(i -> imageMap.put(i,-1));
//        games.stream()
//            .filter(g -> g.getGameMode().equals(GameMode.AnimalIdentification))
//            .map(g -> g.getImg())
//            .filter(i -> imageMap.get(i) == -1)
//            .forEach(i -> {
//                if (imageMap.get(i) == null)
//                    imageMap.put(i, 1);
//                else
//                    imageMap.put(i, imageMap.get(i) + 1);
//            });
//        imageMap.values().stream()
//            .forEach(i -> {
//                ImageStatus status;
//                if (i == -1) status = ImageStatus.PROCESSED;
//                else if (i >= 3) status = ImageStatus.IN_PROCESSING;
//                else status = ImageStatus.NOT_PROCESSED;
//                if (map.get(status) == null) map.put(status,1);
//                else map.put(status,map.get(status)+1);
//            });
//        gameModeStatisticses.add(new ImageSetStatisticsDTO.GameModeStatistics(GameMode.AnimalIdentification, map));
//
//        imageMap.clear();
//        map.clear();
//        refGames.stream()
//            .filter(g -> g.getGameMode().equals(GameMode.TrainingPlantIdentification))
//            .map(g -> g.getImg())
//            .forEach(i -> imageMap.put(i, -1));
//        games.stream()
//            .filter(g -> g.getGameMode().equals(GameMode.PlantIdentification))
//            .map(g -> g.getImg())
//            .filter(i -> imageMap.get(i) == -1)
//            .forEach(i -> {
//                if (imageMap.get(i) == null)
//                    imageMap.put(i, 1);
//                else
//                    imageMap.put(i, imageMap.get(i) + 1);
//            });
//        imageMap.values().stream()
//            .forEach(i -> {
//                ImageStatus status;
//                if (i == -1) status = ImageStatus.PROCESSED;
//                else if (i >= 3) status = ImageStatus.IN_PROCESSING;
//                else status = ImageStatus.NOT_PROCESSED;
//                if (map.get(status) == null) map.put(status, 1);
//                else map.put(status, map.get(status) + 1);
//            });
//        gameModeStatisticses.add(new ImageSetStatisticsDTO.GameModeStatistics(GameMode.PlantIdentification, map));
//
//        result.setGameModeStatistics(gameModeStatisticses);
//
//        return result;
//    }

//    public ImagesStatisticsDTO getImagesStatistics() {
//
//        ImagesStatisticsDTO result = new ImagesStatisticsDTO();
//        List<ImagesStatisticsDTO.GameModeStatistics> gameModeStatisticses = new ArrayList<>();
//
//        List<Game> games = gameRepository.findAll();
//        List<ReferenceGame> refGames = referenceGameRepository.findAll();
//
//        Map<String,Integer> map1 = refGames.stream()
//            .filter(g -> g.getGameMode().equals(GameMode.TrainingAnimalIdentification))
//            .map(g -> g.getImg())
//            .collect(Collectors.toMap(i -> i, i -> -1, (i, j) -> -1));
//        Map<String,Integer> map2 = games.stream()
//                .filter(g -> g.getGameMode().equals(GameMode.AnimalIdentification))
//                .map(g -> g.getImg())
//                .filter(i -> map1.get(i) == -1)
//                .collect(Collectors.toMap(i -> i, i -> 1, (i, j) -> i + j));
//        map2.putAll(map1);
//        map2.values().stream()
//            .collect(Collectors.toMap(i -> {
//                if (i == -1) return ImageStatus.PROCESSED;
//                else if (i >= 3) return ImageStatus.IN_PROCESSING;
//                else return ImageStatus.NOT_PROCESSED;
//            }, i -> 1, (i, j) -> i + j));
//        gameModeStatisticses.add(new ImagesStatisticsDTO.GameModeStatistics(GameMode.AnimalIdentification, map2));
//
//        Map<String,Integer> map3 = refGames.stream()
//            .filter(g -> g.getGameMode().equals(GameMode.TrainingPlantIdentification))
//            .map(g -> g.getImg())
//            .collect(Collectors.toMap(i -> i, i -> -1, (i, j) -> -1));
//        Map<String,Integer> map4 = games.stream()
//            .filter(g -> g.getGameMode().equals(GameMode.PlantIdentification))
//            .map(g -> g.getImg())
//            .filter(i -> map1.get(i) == -1)
//            .collect(Collectors.toMap(i -> i, i -> 1, (i, j) -> i + j));
//        map4.putAll(map3);
//        map4.values().stream()
//            .collect(Collectors.toMap(i -> {
//                if (i == -1) return ImageStatus.PROCESSED;
//                else if (i >= 3) return ImageStatus.IN_PROCESSING;
//                else return ImageStatus.NOT_PROCESSED;
//            }, i -> 1, (i, j) -> i + j));
//        gameModeStatisticses.add(new ImagesStatisticsDTO.GameModeStatistics(GameMode.PlantIdentification, map4));
//
//        result.setGameModeStatistics(gameModeStatisticses);
//
//        return result;
//    }
}
