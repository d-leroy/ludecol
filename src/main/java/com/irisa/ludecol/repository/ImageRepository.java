package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data MongoDB repository for the Image entity.
 */
public interface ImageRepository extends MongoRepository<Image,String> {

    List<Image> findByImageSet(String imageSet);

    Page<Image> findByImageSet(String imageSet, Pageable pageable);

    List<Image> findByNameNotIn(Collection<String> names);

    Image findOneByPath(String path);
}
