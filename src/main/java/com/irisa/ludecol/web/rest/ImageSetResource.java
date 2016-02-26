package com.irisa.ludecol.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.ImageSet;
import com.irisa.ludecol.repository.ImageRepository;
import com.irisa.ludecol.repository.ImageSetRepository;
import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.service.DataExportService;
import com.irisa.ludecol.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Image.
 */
@RestController
@RequestMapping("/api")
public class ImageSetResource {

    private final Logger log = LoggerFactory.getLogger(ImageSetResource.class);

    @Inject
    private ImageSetRepository imageSetRepository;

    @Inject
    private ImageRepository imageRepository;

    @Inject
    private ImageService imageService;

    @Inject
    private DataExportService dataExportService;

    /**
     * POST  /imagesets -> Create a new image set.
     */
    @RequestMapping(value = "/imagesets",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> create(@RequestBody ImageSet imageSet) throws URISyntaxException {
        log.debug("REST request to save ImageSet : {}", imageSet);
        if (imageSet.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new image set cannot already have an ID").build();
        }
        if(imageSetRepository.findByName(imageSet.getName()) != null) {
            return ResponseEntity.badRequest().header("Failure", "An image set already exists with that name").build();
        }
        imageSetRepository.save(imageSet);
        return ResponseEntity.created(new URI("/api/imagesets/" + imageSet.getId())).build();
    }

    /**
     * PUT  /imagesets -> Updates an existing image set.
     */
    @RequestMapping(value = "/imagesets",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> update(@RequestBody ImageSet imageSet) throws URISyntaxException {
        log.debug("REST request to update ImageSet : {}", imageSet);
        if (imageSet.getId() == null) {
            return create(imageSet);
        }
        ImageSet set = imageSetRepository.findOne(imageSet.getId());
        if(set == null) {
            return ResponseEntity.badRequest().header("Failure", "This image set does not exist").build();
        }
        String setName = imageSet.getName();
        if(!setName.equals(set.getName())) {
            if (imageSetRepository.findByName(setName) != null) {
                return ResponseEntity.badRequest().header("Failure", "An image set already exists with that name").build();
            }
            File imagesDir = Paths.get("src/main/webapp/images/"+set.getName()).toFile();
            File newImagesDir = Paths.get("src/main/webapp/images/"+imageSet.getName()).toFile();
            File tilesDir = Paths.get("src/main/webapp/tiles/"+set.getName()).toFile();
            File newTilesDir = Paths.get("src/main/webapp/tiles/"+imageSet.getName()).toFile();

            if(!imagesDir.renameTo(newImagesDir)) {
                return ResponseEntity.badRequest().header("Failure", "The renaming of the image set failed").build();
            } else if(!tilesDir.renameTo(newTilesDir)) {
                newImagesDir.renameTo(imagesDir);
                return ResponseEntity.badRequest().header("Failure", "The renaming of the image set failed").build();
            }
        }
        set.setEnabled(imageSet.getEnabled());
        set.setPriority(imageSet.getPriority());
        set.setRequiredSubmissions(imageSet.getRequiredSubmissions());
        List<Image> images = imageRepository.findByImageSet(set.getName());
        images.stream()
            .forEach(image -> {
                image.setSetPriority(imageSet.getPriority());
                image.setImageSet(setName);
                image.setPath("/tiles/"+setName+"/"+image.getName()+"/");
            });
        set.setName(setName);
        imageRepository.save(images);
        imageSetRepository.save(set);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /imagesets -> get all the image sets.
     */
    @RequestMapping(value = "/imagesets",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public List<ImageSet> getAll() {
        log.debug("REST request to get all ImageSets");
        return imageSetRepository.findAll();
    }

    /**
     * GET  /imagesets/:id -> get the "id" image set.
     */
    @RequestMapping(value = "/imagesets/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<ImageSet> get(@PathVariable String id) {
        log.debug("REST request to get ImageSet : {}", id);
        return Optional.ofNullable(imageSetRepository.findOne(id))
            .map(image -> new ResponseEntity<>(image, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /imagesets/:id/download -> get the data of the "id" image set.
     */
    @RequestMapping(value = "/imagesets/{id}/download",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<DataExportService.DataWrapper> download(@PathVariable String id) {
        log.debug("REST request to get the data of the ImageSet : {}", id);
        return new ResponseEntity<>(dataExportService.exportSet(imageSetRepository.findByName(id)), HttpStatus.OK);
    }

    /**
     * GET  /imagesets/:id/download/animal -> get the data of the "id" image set.
     */
    @RequestMapping(value = "/imagesets/{id}/download/animal",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<DataExportService.DataWrapper> download(@PathVariable String id) {
        log.debug("REST request to get the raw data for animals on the ImageSet: {}", id);
        return new ResponseEntity<>(dataExportService.exportSetAnimal(imageSetRepository.findByName(id)), HttpStatus.OK);
    }

    /**
     * GET  /imagesets/:id/download/plant -> get the data of the "id" image set.
     */
    @RequestMapping(value = "/imagesets/{id}/download/plant",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<DataExportService.DataWrapper> download(@PathVariable String id) {
        log.debug("REST request to get the raw data for plants on the ImageSet : {}", id);
        return new ResponseEntity<>(dataExportService.exportSetPlant(imageSetRepository.findByName(id)), HttpStatus.OK);
    }

    /**
     * DELETE  /imagesets/:id -> delete the "id" image set.
     */
    @RequestMapping(value = "/imagesets/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public void delete(@PathVariable String id) {
        log.debug("REST request to delete ImageSet : {}", id);
        ImageSet imageSet = imageSetRepository.findOne(id);
        //cleaning all references to the images of the image set
        List<Image> images = imageRepository.findByImageSet(imageSet.getName());
        images.stream().forEach(i->imageService.cleanupImage(i));
        Paths.get("src/main/webapp/images/"+imageSet.getName()+"/divided").toFile().delete();
        Paths.get("src/main/webapp/images/"+imageSet.getName()+"/thumbnail").toFile().delete();
        Paths.get("src/main/webapp/images/"+imageSet.getName()).toFile().delete();
        Paths.get("src/main/webapp/tiles/"+imageSet.getName()).toFile().delete();
        //removing the image set from the database
        imageSetRepository.delete(id);
    }
}
