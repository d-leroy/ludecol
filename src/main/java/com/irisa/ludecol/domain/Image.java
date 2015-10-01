package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.irisa.ludecol.domain.subdomain.AnimalSpecies;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageModeStatus;
import com.irisa.ludecol.domain.subdomain.PlantSpecies;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by dorian on 04/05/15.
 */
@Document(collection = "T_IMAGE")
public class Image {

    @Id
    private String id;

    @NotNull
    private String name;

    @NotNull
    private String path;

    @NotNull
    private Integer width;

    @NotNull
    private Integer height;

    @Field("mode_status")
    @JsonProperty("mode_status")
    private EnumMap<GameMode,ImageModeStatus> modeStatus = new EnumMap(GameMode.class);

    @Field("fauna_species")
    @JsonProperty("fauna_species")
    private Set<AnimalSpecies> faunaSpecies = new HashSet<>();

    @Field("flora_species")
    @JsonProperty("flora_species")
    private Set<PlantSpecies> floraSpecies = new HashSet<>();

    @Indexed
    @Field("image_set")
    @JsonProperty("image_set")
    private String imageSet;

    @Field("set_priority")
    @JsonProperty("set_priority")
    private Integer setPriority;

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public EnumMap<GameMode, ImageModeStatus> getModeStatus() {
        return modeStatus;
    }

    public void setModeStatus(EnumMap<GameMode, ImageModeStatus> modeStatus) {
        this.modeStatus = modeStatus;
    }

    public Set<AnimalSpecies> getFaunaSpecies() {
        return faunaSpecies;
    }

    public void setFaunaSpecies(Set<AnimalSpecies> faunaSpecies) {
        this.faunaSpecies = faunaSpecies;
    }

    public Set<PlantSpecies> getFloraSpecies() {
        return floraSpecies;
    }

    public void setFloraSpecies(Set<PlantSpecies> floraSpecies) {
        this.floraSpecies = floraSpecies;
    }

    public String getImageSet() {
        return imageSet;
    }

    public void setImageSet(String imageSet) {
        this.imageSet = imageSet;
    }

    public Integer getSetPriority() {
        return setPriority;
    }

    public void setSetPriority(Integer setPriority) {
        this.setPriority = setPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Image image = (Image) o;

        if ( ! Objects.equals(id, image.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Image{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", path='" + path + '\'' +
            ", width=" + width +
            ", height=" + height +
            ", modeStatus=" + modeStatus +
            ", faunaSpecies=" + faunaSpecies +
            ", floraSpecies=" + floraSpecies +
            ", imageSet='" + imageSet + '\'' +
            ", setPriority=" + setPriority +
            '}';
    }
}
