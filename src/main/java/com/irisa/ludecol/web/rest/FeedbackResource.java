package com.irisa.ludecol.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.irisa.ludecol.domain.Feedback;
import com.irisa.ludecol.repository.FeedbackRepository;
import com.irisa.ludecol.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by dorian on 16/07/15.
 */
@RestController
@RequestMapping("/api")
public class FeedbackResource {

    private final Logger log = LoggerFactory.getLogger(FeedbackResource.class);

    @Inject
    private FeedbackRepository feedbackRepository;

    @RequestMapping(value = "/feedback",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> create(@RequestBody List<Feedback> feedbacks) throws URISyntaxException {
        feedbackRepository.save(feedbacks);
        return ResponseEntity.created(new URI("/api/feedback")).build();
    }

    /**
     * GET  /feedback -> get all feedbacks.
     */
    @RequestMapping(value = "/feedback",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public List<Feedback> getAll() {
        log.debug("REST request to get all Expert games");
        return feedbackRepository.findAll();
    }

}
