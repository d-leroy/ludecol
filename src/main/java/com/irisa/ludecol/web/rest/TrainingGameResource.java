package com.irisa.ludecol.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.TrainingGame;
import com.irisa.ludecol.repository.TrainingGameRepository;
import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.service.ImageProviderService;
import com.irisa.ludecol.service.TrainingGameService;
import com.irisa.ludecol.web.rest.dto.TrainingGameDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class TrainingGameResource {

    private final Logger log = LoggerFactory.getLogger(TrainingGameResource.class);

    @Inject
    private TrainingGameRepository trainingGameRepository;

    @Inject
    private TrainingGameService trainingGameService;

    @Inject
    private ImageProviderService imageProviderService;

    /**
     * POST  /traingames -> Create a new game.
     */
    @RequestMapping(value = "/traingames",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity create(@RequestBody Game game, Principal principal) throws URISyntaxException {
        if(!game.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        log.debug("REST request to save Game : {}", game);
        if (game.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new game cannot already have an ID").build();
        }
        TrainingGameDTO result = trainingGameService.createTrainingGame(game);
        if(result == null)
            return ResponseEntity.badRequest().header("Failure", "No available image were found").build();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(new URI("/api/traingames/" + result.getId()));
        return new ResponseEntity<>(result, responseHeaders, HttpStatus.CREATED);
    }

    /**
     * PUT  /traingames -> Updates an existing game.
     */
    @RequestMapping(value = "/traingames",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<Void> update(@RequestBody Game game, Principal principal) throws URISyntaxException {
        if(!game.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        log.debug("REST request to update Game : {}", game);
        if (game.getId() == null)
            return create(game, principal);
        TrainingGame tGame = trainingGameRepository.findOne(game.getId());
        log.debug("Found training game : {}", tGame);
        trainingGameService.updateTrainingGame(tGame, game);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /traingames -> get all training games.
     */
    @RequestMapping(value = "/traingames",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public List<TrainingGame> getAll() {
        log.debug("REST request to get all Training games");
        return trainingGameRepository.findAll();
    }

    /**
     * GET  /traingames/:id -> get the "id" training game.
     */
    @RequestMapping(value = "/traingames/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<TrainingGameDTO> get(@PathVariable String id, Principal principal) {
        log.debug("REST request to get Image : {}", id);
        TrainingGame result = trainingGameRepository.findOne(id);

        if(result == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if(!result.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        return new ResponseEntity<>(trainingGameService.getTrainingGameWrapper(result), HttpStatus.OK);
    }

}
