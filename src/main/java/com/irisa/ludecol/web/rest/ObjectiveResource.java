package com.irisa.ludecol.web.rest;

import com.irisa.ludecol.domain.Objective;
import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.service.ObjectiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.security.Principal;
import java.util.List;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class ObjectiveResource {

    private final Logger log = LoggerFactory.getLogger(ObjectiveResource.class);

    @Inject
    private ObjectiveService objectiveService;

    /**
     * GET  /notifications/:login -> get all unread notifications for user 'login'.
     */
    @RequestMapping(value = "/objectives",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed(AuthoritiesConstants.USER)
    public DeferredResult<List<Objective>> getUserNotifications(@RequestParam boolean force, Principal principal) {
        log.debug("REST request to get Objectives for user : {}", principal.getName());
        return objectiveService.submitRequest(principal.getName(), force);
    }
}
