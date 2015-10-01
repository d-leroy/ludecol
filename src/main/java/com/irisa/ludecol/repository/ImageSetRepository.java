package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.ImageSet;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Image entity.
 */
public interface ImageSetRepository extends MongoRepository<ImageSet,String> {

    ImageSet findByName(String name);

}
