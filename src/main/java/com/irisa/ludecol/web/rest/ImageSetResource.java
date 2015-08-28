package com.irisa.ludecol.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.ImageSet;
import com.irisa.ludecol.domain.ReferenceGame;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.repository.GameRepository;
import com.irisa.ludecol.repository.ImageRepository;
import com.irisa.ludecol.repository.ImageSetRepository;
import com.irisa.ludecol.repository.ReferenceGameRepository;
import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.service.ImageProviderService;
import com.irisa.ludecol.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * POST  /images -> Create a new image set.
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
     * PUT  /images -> Updates an existing image set.
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
        List<Image> images = imageRepository.findByImageSet(set.getName());
        images.stream()
            .forEach(image -> {
                image.setSetPriority(imageSet.getPriority());
                image.setImageSet(imageSet.getName());
            });
        imageRepository.save(images);
        imageSetRepository.save(imageSet);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /images -> get all the image sets.
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
     * GET  /images/:id -> get the "id" image set.
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
     * DELETE  /images/:id -> delete the "id" image set.
     */
    @RequestMapping(value = "/imagesets/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public void delete(@PathVariable String id) {
        log.debug("REST request to delete ImageSet : {}", id);
        imageSetRepository.delete(id);
    }
}
