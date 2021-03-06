package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.GameResult;
import com.irisa.ludecol.domain.util.CustomGameDeserializer;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;

/**
 * Created by dorian on 04/05/15.
 */
@Document(collection = "T_EXPERT_GAME")
@CompoundIndexes({
    @CompoundIndex(name = "usr_idx", def = "{'game_mode': 1, 'usr': 1}"),
    @CompoundIndex(name = "img_idx", def = "{'game_mode': 1, 'img': 1}")
})
@JsonDeserialize(using = CustomGameDeserializer.class)
public class ExpertGame<T extends GameResult> {

    @Id
    private String id;

    @NotNull
    @Field("usr")
    private String usr;

    @NotNull
    @Field("img")
    private String img;

    @NotNull
    @Field("game_mode")
    @JsonProperty("game_mode")
    private GameMode gameMode;

    @Field("reference_result")
    @JsonProperty("reference_result")
    private T referenceResult;

    @NotNull
    @Field("submitted_result")
    @JsonProperty("submitted_result")
    private T submittedResult;

    @Field("completed")
    private boolean completed;

    @Field("last_modified")
    private DateTime lastModified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public T getReferenceResult() {
        return referenceResult;
    }

    public void setReferenceResult(T referenceResult) {
        this.referenceResult = referenceResult;
    }

    public T getSubmittedResult() {
        return submittedResult;
    }

    public void setSubmittedResult(T submittedResult) {
        this.submittedResult = submittedResult;
    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "ExpertGame{" +
            "id='" + id + '\'' +
            ", usr='" + usr + '\'' +
            ", img='" + img + '\'' +
            ", gameMode=" + gameMode +
            ", referenceResult=" + referenceResult +
            ", submittedResult=" + submittedResult +
            ", completed=" + completed +
            ", lastModified=" + lastModified +
            '}';
    }
}
