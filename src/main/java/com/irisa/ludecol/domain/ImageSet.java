package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by dorian on 21/07/15.
 */
@Document(collection = "T_IMAGESET")
public class ImageSet {

    @Id
    private String id;

    private String name;

    @Field("image_count")
    @JsonProperty("image_count")
    private Integer imageCount;

    private Integer priority;

    @Field("required_submissions")
    @JsonProperty("required_submissions")
    private Integer requiredSubmissions;

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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getRequiredSubmissions() {
        return requiredSubmissions;
    }

    public void setRequiredSubmissions(Integer requiredSubmissions) {
        this.requiredSubmissions = requiredSubmissions;
    }

    @Override
    public String toString() {
        return "ImageSet{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", imageCount=" + imageCount +
            ", priority=" + priority +
            ", requiredSubmissions=" + requiredSubmissions +
            '}';
    }
}
