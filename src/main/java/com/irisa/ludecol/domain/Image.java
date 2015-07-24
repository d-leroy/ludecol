package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.irisa.ludecol.domain.subdomain.AnimalSpecies;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.PlantSpecies;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.util.EnumSet;
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

    @Indexed
    @Field("game_modes")
    @JsonProperty("game_modes")
    private EnumSet<GameMode> gameModes = EnumSet.noneOf(GameMode.class);

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

    public Set<GameMode> getGameModes() {
        return gameModes;
    }

    public void setGameModes(Set<GameMode> gameModes) {
        this.gameModes.clear();
        this.gameModes.addAll(gameModes);
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
            ", gameModes=" + gameModes +
            ", faunaSpecies=" + faunaSpecies +
            ", floraSpecies=" + floraSpecies +
            '}';
    }
}
