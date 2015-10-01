package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.Objective;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the Image entity.
 */
public interface ObjectiveRepository extends MongoRepository<Objective,String> {

    List<Objective> findAllByUsr(String usr);

}
