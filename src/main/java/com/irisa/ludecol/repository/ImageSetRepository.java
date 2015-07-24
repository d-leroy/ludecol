package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.ImageSet;
import com.irisa.ludecol.domain.subdomain.GameMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data MongoDB repository for the Image entity.
 */
public interface ImageSetRepository extends MongoRepository<ImageSet,String> {

    ImageSet findByName(String name);

}
