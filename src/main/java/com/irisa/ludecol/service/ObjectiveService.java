package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.Objective;
import com.irisa.ludecol.repository.ObjectiveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dorian on 01/07/15.
 */
@Service
public class ObjectiveService {

    @Inject
    private ObjectiveRepository objectiveRepository;

    private final Logger log = LoggerFactory.getLogger(ObjectiveService.class);

    private ConcurrentHashMap<String, DeferredResult> objectiveRequests = new ConcurrentHashMap<>();

    /**
     *
     * @param result
     * @param login
     */
    private void setResult(final DeferredResult result, final String login) {
        List<Objective> objectives = objectiveRepository.findAllByUsr(login);
        List<Objective> ongoing = new ArrayList<>();
        List<Objective> pending = new ArrayList<>();
        objectives.stream()
            .filter(objective -> objective.getNbCompletedGames() < objective.getNbGamesToComplete())
            .sorted((o1, o2) -> (int) Math.signum(o1.getCreationDate().getMillis() - o2.getCreationDate().getMillis()))
            .forEach(o -> ongoing.add(o));
        objectives.stream()
            .filter(objective -> objective.getNbCompletedGames() == objective.getNbGamesToComplete())
            .sorted((o1, o2) -> (int) Math.signum(o1.getCreationDate().getMillis() - o2.getCreationDate().getMillis()))
            .forEach(o -> pending.add(o));
        ongoing.addAll(pending);
        result.setResult(ongoing);
        objectiveRequests.remove(login);
    }

    public void cancelRequest(String login) {
        DeferredResult result = objectiveRequests.get(login);
        if(result != null) {
            result.setErrorResult(Collections.emptyList());
            objectiveRequests.remove(result);
        }
    }

    /**
     *
     * @param login
     */
    public void handleObjectiveUpdate(String login) {
        DeferredResult result = objectiveRequests.get(login);
        if (result != null) {
            setResult(result,login);
        }
    }

    /**
     *
     * @param login
     * @param force
     * @return
     */
    public DeferredResult<List<Objective>> submitRequest(String login, boolean force) {

        final DeferredResult<List<Objective>> result;

        DeferredResult currentValue = objectiveRequests.get(login);

        if(currentValue != null) {
            //If a request had already been made without being completed, return an empty result.
            objectiveRequests.remove(currentValue);
            currentValue.setResult(Collections.emptyList());
        }

        result = new DeferredResult<>(null, Collections.emptyList());
        objectiveRequests.put(login, result);

        result.onCompletion(() -> objectiveRequests.remove(result));
        result.onTimeout(() -> setResult(result,login));

        if(force)
            setResult(result,login);

        return result;
    }

}
