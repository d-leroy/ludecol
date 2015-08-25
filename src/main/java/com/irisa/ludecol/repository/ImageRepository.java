package com.irisa.ludecol.repository;

import com.irisa.ludecol.domain.Image;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageModeStatus;
import com.irisa.ludecol.domain.subdomain.ImageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data MongoDB repository for the Image entity.
 */
public interface ImageRepository extends MongoRepository<Image,String> {

    @Query(value="{ 'mode_status.?1.mode' : '?0' }")
    List<Image> findByModeStatus(GameMode mode, ImageStatus status);

    List<Image> findByGameModesContaining(GameMode gameMode);

    List<Image> findByImageSet(String imageSet);

    Page<Image> findByImageSet(String imageSet, Pageable pageable);

    List<Image> findByNameNotIn(Collection<String> names);

    Image findOneByPath(String path);
}
