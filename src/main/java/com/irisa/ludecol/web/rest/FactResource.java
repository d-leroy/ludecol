package com.irisa.ludecol.web.rest;

import com.irisa.ludecol.security.AuthoritiesConstants;
import com.irisa.ludecol.web.rest.util.UnicodeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import java.io.*;
import java.nio.charset.Charset;
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
        FileInputStream fis = null;
        UnicodeReader ur = null;
        BufferedReader in = null;
        try {
            fis = new FileInputStream(new File("tipsFR"));
            ur = new UnicodeReader(fis, "UTF-8");
            in = new BufferedReader(ur);
            List<String> lines = in.lines().collect(Collectors.toList());
            result = lines.get(rand.nextInt(lines.size()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis != null) {try {fis.close();} catch (IOException e) {}}
            if(ur != null) {try {ur.close();} catch (IOException e) {}}
            if(in != null) {try {in.close();} catch (IOException e) {}}
        }
        return Collections.singletonMap("response", result);
    }
}
