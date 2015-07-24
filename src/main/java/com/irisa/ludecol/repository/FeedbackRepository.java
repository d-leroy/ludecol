package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.Feedback;
import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.subdomain.GameMode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data MongoDB repository for the Image entity.
 */
public interface FeedbackRepository extends MongoRepository<Feedback,String> {


}
