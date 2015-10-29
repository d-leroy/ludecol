package com.irisa.ludecol.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageModeStatus;
import com.irisa.ludecol.repository.GameRepository;
import com.irisa.ludecol.repository.ImageRepository;
import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.service.GameProcessingService;
import com.irisa.ludecol.service.ImageProviderService;
import com.irisa.ludecol.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
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
import java.util.Optional;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class GameResource {

    private final Logger log = LoggerFactory.getLogger(GameResource.class);

    @Inject
    private GameRepository gameRepository;

    @Inject
    private ImageProviderService imageProviderService;

    @Inject
    private ImageRepository imageRepository;

    @Inject
    private GameProcessingService gameProcessingService;

    @Inject
    private UserService userService;

    /**
     * POST  /games -> Create a new game.
     */
    @RequestMapping(value = "/games",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity create(@RequestBody Game game, Principal principal) throws URISyntaxException {
        if(!game.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        log.debug("REST request to save Game : {}", game);
        if (game.getId() != null)
            return ResponseEntity.badRequest().header("Failure", "A new game cannot already have an ID").build();
        Image img = imageProviderService.findImage(game.getGameMode(), game.getUsr());
        if(img == null)
            return ResponseEntity.badRequest().header("Failure", "No available image were found").build();
        ImageModeStatus status = img.getModeStatus().get(game.getGameMode());
        status.setGameNumber(status.getGameNumber() + 1);
        imageRepository.save(img);
        game.setImg(img.getId());
        gameRepository.save(game);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(new URI("/api/games/" + game.getId()));
        return new ResponseEntity<>(game, responseHeaders, HttpStatus.CREATED);
    }

    /**
     * PUT  /games -> Updates an existing game.
     */
    @RequestMapping(value = "/games",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<Void> update(@RequestBody Game game, Principal principal) throws URISyntaxException {
        log.debug("REST request to update Game : {}", game);
        if(!game.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        if (game.getId() == null)
            return create(game, principal);
        gameRepository.save(game);
        if(game.getCompleted())
            gameProcessingService.processGame(game);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /games/:id -> get the 'id' game.
     */
    @RequestMapping(value = "/games/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<Game> get(@PathVariable String id, Principal principal) {
        log.debug("REST request to get Game {}", id);
        Game result = gameRepository.findOne(id);
        if(result == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if(!result.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * GET  /games/:id -> get all games.
     */
    @RequestMapping(value = "/games",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Iterable<Game>> getAll() {
        List<Game> result = gameRepository.findAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * DELETE /games -> deletes the game if it is not completed and adds the image tp the "skipped images" list of the user
     */
    @RequestMapping(value = "/games/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<Void> delete(@PathVariable String id, Principal principal) {
        Game game = gameRepository.findOne(id);
        if(!game.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        if(game.getCompleted())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Image img = imageRepository.findOne(game.getImg());
        ImageModeStatus modeStatus = img.getModeStatus().get(game.getGameMode());
        if(modeStatus.getGameNumber() > 0) {
            modeStatus.setGameNumber(modeStatus.getGameNumber()-1);
        }
        imageRepository.save(img);
        userService.updateSkippedList(game.getUsr(),game.getImg(),game.getGameMode());
        gameRepository.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /games -> get the last 5 completed games.
     */
    @RequestMapping(value = "/games/last",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<List<Game>> getLast5(Principal principal) {
        log.debug("REST request to get the last 5 Games of user : {}", principal.getName());
        List<Game> result = gameRepository.findFirst5ByUsrAndCompletedAndScoreGreaterThan(principal.getName(), true, -1, new Sort(Sort.Direction.DESC, "last_modified"));
        return Optional.ofNullable(result)
            .map(list -> new ResponseEntity<>(list, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }



    /**
     * GET  /games/image/:img -> get all games on the "img" image.
     */
//    @RequestMapping(value = "/games/image/{img}",
//        method = RequestMethod.GET,
//        produces = MediaType.APPLICATION_JSON_VALUE)
//    @Timed
//    @RolesAllowed(AuthoritiesConstants.USER)
//    ResponseEntity<List<Game>> getImageGames(@PathVariable String img) {
//        log.debug("REST request to get User : {}", img);
//        return new ResponseEntity<>(gameRepository.findAllByImg(img), HttpStatus.OK);
//    }
}
