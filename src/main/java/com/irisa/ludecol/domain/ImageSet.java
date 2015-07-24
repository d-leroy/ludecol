package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by dorian on 21/07/15.
 */
public class ImageSet {

    @Id
    private String id;

    private String name;

    @Field("image_count")
    @JsonProperty("image_count")
    private Integer imageCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getImageCount() {
        return imageCount;
    }

    public void setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }

    @Override
    public String toString() {
        return "ImageSet{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", imageCount=" + imageCount +
            '}';
    }
}
