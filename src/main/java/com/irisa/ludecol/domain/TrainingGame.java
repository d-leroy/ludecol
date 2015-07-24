package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.GameResult;
import com.irisa.ludecol.domain.util.CustomGameDeserializer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;

/**
 * Created by dorian on 04/05/15.
 */
@Document(collection = "T_TRAINING_GAME")
@CompoundIndexes({
    @CompoundIndex(name = "usr_idx", def = "{'game_mode': 1, 'usr': 1}"),
    @CompoundIndex(name = "img_idx", def = "{'game_mode': 1, 'img': 1}")
})
@JsonDeserialize(using = CustomGameDeserializer.class)
public class TrainingGame<T extends GameResult> {

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

    @NotNull
    @Field("submitted_result")
    @JsonProperty("submitted_result")
    private T submittedResult;

    @NotNull
    @JsonIgnore
    @Field("reference_result")
    @JsonProperty("reference_result")
    private T referenceResult;

    @NotNull
    @Field("score")
    private Integer score;

    @Field("completed")
    private boolean completed;

    public String getId() {
        return id;
    }

    public String getUsr() {
        return usr;
    }

    public String getImg() {
        return img;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public T getSubmittedResult() {
        return submittedResult;
    }

    public T getReferenceResult() {
        return referenceResult;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void setSubmittedResult(T submittedResult) {
        this.submittedResult = submittedResult;
    }

    public void setReferenceResult(T referenceResult) {
        this.referenceResult = referenceResult;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "TrainingGame{" +
            "id=" + id +
            ", usr='" + usr + "'" +
            ", img='" + img + "'" +
            ", game_mode='" + gameMode + "'" +
            ", game_result='" + submittedResult + "'" +
            ", reference_game='" + referenceResult + "'" +
            ", score='" + score + "'" +
            '}';
    }
}
