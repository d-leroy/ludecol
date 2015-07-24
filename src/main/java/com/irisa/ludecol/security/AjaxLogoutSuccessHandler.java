package com.irisa.ludecol.security;

import com.irisa.ludecol.service.GameNotificationService;
import com.irisa.ludecol.service.ObjectiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Spring Security logout handler, specialized for Ajax requests.
 */
@Component
public class AjaxLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler
        implements LogoutSuccessHandler {

    private final Logger log = LoggerFactory.getLogger(AjaxLogoutSuccessHandler.class);

    @Inject
    private GameNotificationService gameNotificationService;

    @Inject
    private ObjectiveService objectiveService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication)
            throws IOException, ServletException {
        log.debug("User logged out : {}", authentication.getName());
        gameNotificationService.cancelRequest(authentication.getName());
        objectiveService.cancelRequest(authentication.getName());
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
