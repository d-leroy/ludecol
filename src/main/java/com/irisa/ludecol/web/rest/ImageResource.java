package com.irisa.ludecol.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.ImageSet;
import com.irisa.ludecol.repository.GameRepository;
import com.irisa.ludecol.repository.ImageRepository;
import com.irisa.ludecol.repository.ImageSetRepository;
import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.service.ImageProviderService;
import com.irisa.ludecol.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Image.
 */
@RestController
@RequestMapping("/api")
public class ImageResource {

    private final Logger log = LoggerFactory.getLogger(ImageResource.class);

    @Inject
    private ImageRepository imageRepository;

    @Inject
    private ImageSetRepository imageSetRepository;

    @Inject
    private ImageService imageService;

    @Inject
    private ImageProviderService imageProviderService;

    @Inject
    private GameRepository gameRepository;

//    @Inject
//    private ReferenceGameRepository referenceGameRepository;

    @RequestMapping(value = "/images",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> handleFileUpload(@RequestParam("cols") int cols,
                                                 @RequestParam("rows") int rows,
                                                 @RequestParam("set") String set,
                                                 @RequestParam("name") String name,
                                                 @RequestParam("file") MultipartFile file){
        log.debug("=======================Received file=======================");
        if (!file.isEmpty()) {
            try {
                Paths.get("src/main/webapp/images/" + set).toFile().mkdirs();
                byte[] bytes = file.getBytes();
                log.debug("Path to saved file : {}","src/main/webapp/images/" + set + "/" + name);
                File res = Paths.get("src/main/webapp/images/" + set + "/" + name).toFile();
                BufferedOutputStream stream =
                    new BufferedOutputStream(new FileOutputStream(res));
                stream.write(bytes);
                stream.close();
                ImageSet imageSet = imageSetRepository.findByName(set);
                imageService.splitImage(cols, rows, set, res).stream().forEach(f -> {
                    imageService.thumbnailize(f,set);
                    imageService.zoomifyImage(f,set);
                    imageService.insertImage(f,imageSet);
                    f.delete();
                });
                res.delete();
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT  /images -> updates an existing image.
     */
    @RequestMapping(value = "/images",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> update(@RequestBody Image image) throws URISyntaxException {
        log.debug("REST request to update Image : {}", image);
        if (image.getId() == null) {
            return ResponseEntity.badRequest().header("Failure", "Cannot update an Image without an ID").build();
        }
        imageRepository.save(image);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /images -> get the list of images satisfying the request.
     */
    @RequestMapping(value = "/images",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Iterable<Image>> getAll(HttpServletRequest request) {
        ResponseEntity<Iterable<Image>> result;
        String page = request.getParameter("page");
        String set = request.getParameter("set");
        if(page != null) {
            int pageNumber = 0;
            try {
                pageNumber = Math.max(0,Integer.parseInt(page));
            }
            catch(NumberFormatException e) {
                e.printStackTrace();
            }
            finally {
                PageRequest pageRequest = new PageRequest(pageNumber,12);
                if(set == null) {
                    log.debug("REST request to get page number {} of Images", pageNumber);
                    result = new ResponseEntity<>(imageRepository.findAll(pageRequest), HttpStatus.OK);
                }
                else {
                    log.debug("REST request to get page number {} of Images from set : {}", pageNumber, set);
                    result = new ResponseEntity<>(imageRepository.findByImageSet(set,pageRequest), HttpStatus.OK);
                }
            }
        }
        else {
            if(set == null) {
                log.debug("REST request to get all Images");
                result = new ResponseEntity<>(imageRepository.findAll(), HttpStatus.OK);
            }
            else {
                log.debug("REST request to get all Images from set : {}", set);
                result = new ResponseEntity<>(imageRepository.findByImageSet(set), HttpStatus.OK);
            }
        }
        return result;
    }

    /**
     * GET  /images/:id -> get the "id" image.
     */
    @RequestMapping(value = "/images/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<Image> get(@PathVariable String id) {
        log.debug("REST request to get Image : {}", id);
        return Optional.ofNullable(imageRepository.findOne(id))
            .map(image -> new ResponseEntity<>(image, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /images/:id/games -> get all games on the "id" image.
     */
    @RequestMapping(value = "/images/{id}/games",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    ResponseEntity<List<Game>> getImageGames(@PathVariable String id) {
        log.debug("REST request to get User : {}", id);
        return new ResponseEntity<>(gameRepository.findAllByImg(id), HttpStatus.OK);
    }

    /**
     * DELETE  /images/:id -> delete the "id" image.
     */
    @RequestMapping(value = "/images/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public void delete(@PathVariable String id) {
        log.debug("REST request to delete Image : {}", id);
        gameRepository.findAllByImg(id).stream().forEach(g->g.setImg("-1"));

        imageRepository.delete(id);

    }

//    /**
//     * GET  /images/statistics -> get statistics on all images.
//     */
//    @RequestMapping(value = "/images/statistics",
//        method = RequestMethod.GET,
//        produces = MediaType.APPLICATION_JSON_VALUE)
//    @Timed
//    @RolesAllowed(AuthoritiesConstants.ADMIN)
//    ResponseEntity<List<ReferenceGame>> getImageReferenceGames(HttpServletRequest request, @PathVariable String id) {
//        log.debug("REST request to get User : {}", id);
//        String mode = request.getParameter("mode");
//        List result = new ArrayList<>();
//        if(mode != null) {
//            result.add(referenceGameRepository.findByImgAndGameMode(id,GameMode.valueOf(mode)));
//        }
//        else {
//            result.addAll(referenceGameRepository.findAllByImg(id));
//        }
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }
}
