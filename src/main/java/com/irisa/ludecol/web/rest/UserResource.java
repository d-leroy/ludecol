package com.irisa.ludecol.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.irisa.ludecol.domain.ExpertGame;
import com.irisa.ludecol.domain.Game;
import com.irisa.ludecol.domain.TrainingGame;
import com.irisa.ludecol.domain.User;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.repository.ExpertGameRepository;
import com.irisa.ludecol.repository.GameRepository;
import com.irisa.ludecol.repository.TrainingGameRepository;
import com.irisa.ludecol.repository.UserRepository;
import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.service.ImageProviderService;
import com.irisa.ludecol.service.TrainingGameService;
import com.irisa.ludecol.service.UserService;
import com.irisa.ludecol.web.rest.dto.TrainingGameDTO;
import com.irisa.ludecol.web.rest.dto.UserStatisticsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private TrainingGameRepository trainingGameRepository;

    @Inject
    private GameRepository gameRepository;

    @Inject
    private ExpertGameRepository expertGameRepository;

    @Inject
    private ImageProviderService imageProviderService;

    @Inject
    private TrainingGameService trainingGameService;

    @Inject
    private UserService userService;

    /**
     * GET  /users -> get all users.
     */
    @RequestMapping(value = "/users",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public List<User> getAll() {
        log.debug("REST request to get all Users");
        return userRepository.findAll();
    }

    /**
     * GET  /users/:login -> get the "login" user.
     */
    //TODO allow any user to access other users profile?
    @RequestMapping(value = "/users/{login}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<User> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);
        return userRepository.findOneByLogin(login)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /user/:login/traingames -> get all training games for the "login" user.
     */
    @RequestMapping(value = "/users/traingames",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<List<TrainingGameDTO>> getTrainingGames(HttpServletRequest request, Principal principal) {
        log.debug("REST request to get all Training qames for User : {}", principal.getName());
        String completed = request.getParameter("completed");
        String mode = request.getParameter("mode");
        List<TrainingGame> result;
        if(completed != null && mode != null) {
            boolean isCompleted = false;
            if(completed.equals("true")) {
                isCompleted = true;
            }
            result = trainingGameRepository.findAllByUsrAndGameModeAndCompleted(principal.getName(), GameMode.valueOf(mode), isCompleted);
        }
        else if(completed != null) {
            boolean isCompleted = false;
            if(completed.equals("true")) {
                isCompleted = true;
            }
            result = trainingGameRepository.findAllByUsrAndCompleted(principal.getName(), isCompleted);
        }
        else if(mode != null) {
            result = trainingGameRepository.findAllByUsrAndGameMode(principal.getName(), GameMode.valueOf(mode));
        }
        else {
            result = trainingGameRepository.findAllByUsr(principal.getName());
        }
        List<TrainingGameDTO> wrappers = new ArrayList<>();
        for(TrainingGame game : result) {
            wrappers.add(trainingGameService.getTrainingGameWrapper(game));
        }
        return new ResponseEntity<>(wrappers, HttpStatus.OK);
    }

    /**
     * GET  /user/:login/games -> get all games from the "login" user.
     */
    @RequestMapping(value = "/users/games",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<List<Game>> getUserGames(HttpServletRequest request, Principal principal) {
        log.debug("REST request to get all games for User : {}", principal.getName());
        String completed = request.getParameter("completed");
        String mode = request.getParameter("mode");

        List<Game> result;
        if(completed != null && mode != null) {
            boolean isCompleted = false;
            if(completed.equals("true")) {
                isCompleted = true;
            }
            result = gameRepository.findAllByUsrAndGameModeAndCompleted(principal.getName(), GameMode.valueOf(mode), isCompleted);
        }
        else if(completed != null) {
            boolean isCompleted = false;
            if(completed.equals("true")) {
                isCompleted = true;
            }
            result = gameRepository.findAllByUsrAndCompleted(principal.getName(), isCompleted);
        }
        else if(mode != null) {
            result = gameRepository.findAllByUsrAndGameMode(principal.getName(), GameMode.valueOf(mode));
        }
        else {
            result = gameRepository.findAllByUsr(principal.getName());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * GET  /user/:login/pagedgames -> get the request page of games from the "login" user.
     */
    @RequestMapping(value = "/users/pagedgames",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<Page<Game>> getUserPagedGames(HttpServletRequest request, Principal principal) {
        String completed = request.getParameter("completed");
        String mode = request.getParameter("mode");
        String page = request.getParameter("page");
        int pageNumber = 0;
        if(page != null) {try {pageNumber = Math.max(0,Integer.parseInt(page));} catch(NumberFormatException e) {}}
        PageRequest pageRequest = new PageRequest(pageNumber,10,new Sort(Sort.Direction.DESC,"last_modified"));

        log.debug("REST request to get page number {} of corrected games for User : {}", pageNumber, principal.getName());

        Page<Game> result;
        if(completed != null && mode != null) {
            boolean isCompleted = false;
            if(completed.equals("true")) {
                isCompleted = true;
            }
            result = gameRepository.findByUsrAndGameModeAndCompletedAndScoreGreaterThan(principal.getName(), GameMode.valueOf(mode), isCompleted, -1, pageRequest);
        }
        else if(completed != null) {
            boolean isCompleted = false;
            if(completed.equals("true")) {
                isCompleted = true;
            }
            result = gameRepository.findByUsrAndCompletedAndScoreGreaterThan(principal.getName(), isCompleted, -1, pageRequest);
        }
        else if(mode != null) {
            result = gameRepository.findByUsrAndGameModeAndScoreGreaterThan(principal.getName(), GameMode.valueOf(mode), -1, pageRequest);
        }
        else {
            result = gameRepository.findByUsrAndScoreGreaterThan(principal.getName(), -1, pageRequest);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * GET  /user/:login/expertgames -> get all expert games from the "login" user.
     */
    @RequestMapping(value = "/users/expertgames",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<List<ExpertGame>> getUserExpertGames(HttpServletRequest request, Principal principal) {
        log.debug("REST request to get all Expert games for User : {}", principal.getName());
        String completed = request.getParameter("completed");
        String mode = request.getParameter("mode");

        List<ExpertGame> result;
        if(completed != null && mode != null) {
            boolean isCompleted = false;
            if(completed.equals("true")) {
                isCompleted = true;
            }
            result = expertGameRepository.findAllByUsrAndGameModeAndCompleted(principal.getName(), GameMode.valueOf(mode), isCompleted);
        }
        else if(completed != null) {
            boolean isCompleted = false;
            if(completed.equals("true")) {
                isCompleted = true;
            }
            result = expertGameRepository.findAllByUsrAndCompleted(principal.getName(), isCompleted);
        }
        else if(mode != null) {
            result = expertGameRepository.findAllByUsrAndGameMode(principal.getName(), GameMode.valueOf(mode));
        }
        else {
            result = expertGameRepository.findAllByUsr(principal.getName());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * GET  /user/statistics -> get statistics for the current user.
     */
    @RequestMapping(value = "/users/statistics",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(Principal principal) {
        return new ResponseEntity<>(userService.getUserStatistics(principal.getName()), HttpStatus.OK);
    }
}
