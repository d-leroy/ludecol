package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Image entity.
 */
public interface FeedbackRepository extends MongoRepository<Feedback,String> {


}
