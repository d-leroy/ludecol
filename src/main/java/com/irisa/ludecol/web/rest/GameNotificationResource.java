package com.irisa.ludecol.web.rest;

import com.irisa.ludecol.domain.GameNotification;
import com.irisa.ludecol.repository.GameNotificationRepository;
import com.irisa.ludecol.repository.UserRepository;
import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.service.GameNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class GameNotificationResource {

    private final Logger log = LoggerFactory.getLogger(GameNotificationResource.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private GameNotificationRepository gameNotificationRepository;

    @Inject
    private GameNotificationService gameNotificationService;

    /**
     * GET  /notifications/:login -> get all unread notifications for user 'login'.
     */
    @RequestMapping(value = "/notifications",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed(AuthoritiesConstants.USER)
    public DeferredResult<List<GameNotification>> getUserNotifications(@RequestParam boolean force, Principal principal) {
        log.debug("REST request to get unread Notifications for user : {}", principal.getName());
        return gameNotificationService.submitRequest(principal.getName(),force);
    }

    /**
     * PUT  /notifications -> mark the given notification as read.
     */
    @RequestMapping(value = "/notifications",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<Void> markAsRead(@RequestBody GameNotification notif, Principal principal) {
        if(notif == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        GameNotification notification = gameNotificationRepository.findOne(notif.getId());
        if(notification == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if(!notification.getUsr().equals(principal.getName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        notification.setViewed(true);
        gameNotificationRepository.save(notification);
        log.debug("Saved notification : {}", notification);
        return ResponseEntity.ok().build();
    }
}
