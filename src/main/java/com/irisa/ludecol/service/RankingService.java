package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.User;
import com.irisa.ludecol.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.LinkedList;

/**
 * Created by dorian on 02/07/15.
 */
@Service
public class RankingService {

    @Inject
    private UserRepository userRepository;

    public void rankPlayers() {

        LinkedList<User> users = new LinkedList<>();
        users.addAll(userRepository.findAll(new Sort(Sort.Direction.DESC, "score")));

        int nbUserPerRank = users.size() / 50;

        for(int i=1; i<50; i++) {
            for(int j=0; j<nbUserPerRank; j++) {
                User user = users.poll();
                user.setRank(i);
                user.setScore((50-i)*1000);
            }
        }
        for(User user : users) {
            user.setRank(50);
            user.setScore(0);
        }

    }

}
