package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.GameNotification;
import com.irisa.ludecol.repository.GameNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dorian on 01/07/15.
 */
@Service
public class GameNotificationService {

    @Inject
    private GameNotificationRepository gameNotificationRepository;

    private final Logger log = LoggerFactory.getLogger(GameNotificationService.class);

    private ConcurrentHashMap<String, DeferredResult> notificationRequests = new ConcurrentHashMap<>();

    /**
     *
     * @param result
     * @param login
     * @param force
     */
    private void setResult(final DeferredResult result, final String login, boolean force) {
        if(force) {
            //Set result only if there are non-viewed notifications (Resend already sent notifications)
            List<GameNotification> list = gameNotificationRepository.findAllByUsrAndViewed(login,false);

            list.removeIf(notification -> notification.getViewed());
            if (!list.isEmpty()) {
                result.setResult(list);
                notificationRequests.remove(result);
            }
        }
        else {
            //Set result only if there are non-sent notifications
            List<GameNotification> list = gameNotificationRepository.findAllByUsrAndSent(login,false);

            list.removeIf(notification -> notification.getSent());
            if (!list.isEmpty()) {
                result.setResult(list);
                notificationRequests.remove(result);
            }
        }
    }

    public void cancelRequest(String login) {
        DeferredResult result = notificationRequests.get(login);
        if(result != null) {
            result.setErrorResult(Collections.emptyList());
            notificationRequests.remove(result);
        }
    }

    /**
     *
     * @param login
     */
    public void handleNewNotification(String login) {
        DeferredResult result = notificationRequests.get(login);
        if (result != null) {
            setResult(result,login,false);
        }
    }

    /**
     *
     * @param login
     * @param force
     * @return
     */
    public DeferredResult<List<GameNotification>> submitRequest(String login, boolean force) {

        final DeferredResult<List<GameNotification>> result;

        DeferredResult currentValue = notificationRequests.get(login);

        if(currentValue != null) {
            //If a request had already been made without being completed, return an empty result.
            notificationRequests.remove(currentValue);
            currentValue.setResult(Collections.emptyList());
        }

        result = new DeferredResult<>(null, Collections.emptyList());
        notificationRequests.put(login, result);

        result.onCompletion(() -> {
            List<GameNotification> list = (List<GameNotification>) result.getResult();
            for (GameNotification notification : list) {
                if (!notification.getSent()) {
                    notification.setSent(true);
                    gameNotificationRepository.save(notification);
                }
            }
            notificationRequests.remove(result);
        });

        result.onTimeout(() -> notificationRequests.remove(result));

        setResult(result, login, force);

        return result;
    }



}
