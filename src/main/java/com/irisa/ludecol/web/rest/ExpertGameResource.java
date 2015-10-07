package com.irisa.ludecol.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.irisa.ludecol.domain.ExpertGame;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.repository.ExpertGameRepository;
import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.service.ExpertGameService;
import com.irisa.ludecol.service.GameProcessingService;
import com.irisa.ludecol.service.ImageProviderService;
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

//import com.irisa.ludecol.repository.ProcessedGameRepository;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class ExpertGameResource {

    private final Logger log = LoggerFactory.getLogger(ExpertGameResource.class);

    @Inject
    private ExpertGameRepository expertGameRepository;

    @Inject
    private ExpertGameService expertGameService;

    @Inject
    private ImageProviderService imageProviderService;

    /**
     * POST  /expertgames -> Create a new expert game.
     */
    @RequestMapping(value = "/expertgames",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity create(@RequestBody Game game, Principal principal) throws URISyntaxException {
        if(!game.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        log.debug("REST request to save Expert game : {}", game);
        if (game.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new game cannot already have an ID").build();
        }
        ExpertGame result = expertGameService.createExpertGame(game);
        if (result == null)
            return ResponseEntity.badRequest().header("Failure", "No available image were found").build();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(new URI("/api/expertgames/" + result.getId()));
        return new ResponseEntity<>(result, responseHeaders, HttpStatus.CREATED);
    }

    /**
     * PUT  /expertgames -> Updates an existing expert game.
     */
    @RequestMapping(value = "/expertgames",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<Void> update(@RequestBody Game game, Principal principal) throws URISyntaxException {
        if(!game.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        log.debug("REST request to update Expert game : {}", game);
        if (game.getId() == null) {
            return create(game,principal);
        }
        ExpertGame expertGame = expertGameRepository.findOne(game.getId());
        expertGame.setCompleted(game.getCompleted());
        expertGame.setSubmittedResult(game.getGameResult());
        expertGameRepository.save(expertGame);
        if(expertGame.getCompleted()) {
            expertGameService.handleExpertGameSubmission(expertGame);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /expertgames -> get all expert games.
     */
    @RequestMapping(value = "/expertgames",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public List<ExpertGame> getAll() {
        log.debug("REST request to get all Expert games");
        return expertGameRepository.findAll();
    }

    /**
     * GET  /expertgames/:id -> get the "id" expert game.
     */
    @RequestMapping(value = "/expertgames/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<ExpertGame> get(@PathVariable String id, Principal principal) {
        log.debug("REST request to get Expert game : {}", id);
        ExpertGame result = expertGameRepository.findOne(id);
        if(result == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if(!result.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
