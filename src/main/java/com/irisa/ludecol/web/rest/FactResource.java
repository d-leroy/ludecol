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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class FactResource {

    private final Logger log = LoggerFactory.getLogger(FactResource.class);

    private Random rand = new Random();

    /**
     * GET  /fact -> gets a random ecological fact.
     */
    @RequestMapping(value = "/fact",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed(AuthoritiesConstants.USER)
    public Map getFact() {
        log.debug("REST request to get a random fact");
        String result = "<fun fact here>";
        try {
            BufferedReader reader = new BufferedReader(new FileReader("tips"));
            List<String> lines = reader.lines().collect(Collectors.toList());
            result = lines.get(rand.nextInt(lines.size()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Collections.singletonMap("response", result);
    }
}
