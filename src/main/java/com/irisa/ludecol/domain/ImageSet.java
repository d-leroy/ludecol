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

    private Integer priority;

    @Field("required_submissions")
    @JsonProperty("required_submissions")
    private Integer requiredSubmissions;

    private Boolean enabled = true;

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "ImageSet{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", priority=" + priority +
            ", requiredSubmissions=" + requiredSubmissions +
            ", enabled=" + enabled +
            '}';
    }
}
