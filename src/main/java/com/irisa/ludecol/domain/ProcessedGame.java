package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ProcessedGameResult;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;

/**
 * Created by dorian on 04/05/15.
 */
@Document(collection = "T_PROCESSED_GAME")
@CompoundIndexes({
    @CompoundIndex(name = "img_idx", def = "{'img': 1, 'game_mode': 1}")
})
public class ProcessedGame<T extends ProcessedGameResult> {

    @Id
    private String id;

    @NotNull
    @Field("img")
    private String img;

    @NotNull
    @Field("game_mode")
    @JsonProperty("game_mode")
    private GameMode gameMode;

    @NotNull
    @Field("processed_game_result")
    @JsonProperty("processed_game_result")
    private T processedGameResult;

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    @JsonIgnore
    public T getProcessedGameResult() {
        return processedGameResult;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void setProcessedGameResult(T processedGameResult) {
        this.processedGameResult = processedGameResult;
    }

    @Override
    public String toString() {
        return "ProcessedGame{" +
            "id=" + id +
            ", img='" + img + "'" +
            ", game_mode='" + gameMode + "'" +
            ", processed_game_result='" + processedGameResult + "'" +
            '}';
    }
}
