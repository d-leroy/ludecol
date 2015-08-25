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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;

/**
 * Created by dorian on 04/05/15.
 */
@Document(collection = "T_GAME")
@CompoundIndexes({
    @CompoundIndex(name = "usr_idx", def = "{'game_mode': 1, 'usr': 1}"),
    @CompoundIndex(name = "img_idx", def = "{'game_mode': 1, 'img': 1}")
})
@JsonDeserialize(using = CustomGameDeserializer.class)
public class Game<T extends GameResult> {

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
    @Field("game_result")
    @JsonProperty("game_result")
    private T gameResult;

    @Field("corrected_game_result")
    @JsonProperty("corrected_game_result")
    private T correctedGameResult;

    @Field("completed")
    private boolean completed;

    @Indexed
    @Field("last_modified")
    @JsonProperty("last_modified")
    private DateTime lastModified;

    @Field("score")
    @JsonProperty("score")
    private int score=-1;

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

    public T getGameResult() {
        return gameResult;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUser(String usr) {
        this.usr = usr;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void setGameResult(T gameResult) {
        this.gameResult = gameResult;
    }

    public T getCorrectedGameResult() {
        return correctedGameResult;
    }

    public void setCorrectedGameResult(T correctedGameResult) {
        this.correctedGameResult = correctedGameResult;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Game{" +
            "id='" + id + '\'' +
            ", usr='" + usr + '\'' +
            ", img='" + img + '\'' +
            ", gameMode=" + gameMode +
            ", gameResult=" + gameResult +
            ", correctedGameResult=" + correctedGameResult +
            ", completed=" + completed +
            ", lastModified=" + lastModified +
            ", score=" + score +
            '}';
    }
}
