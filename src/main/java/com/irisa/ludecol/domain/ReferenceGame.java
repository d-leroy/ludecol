package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.GameResult;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;

/**
 * Created by dorian on 04/05/15.
 */
@Document(collection = "T_REFERENCE_GAME")
@CompoundIndexes({
    @CompoundIndex(name = "img_idx", def = "{'img': 1, 'game_mode': 1}")
})
public class ReferenceGame<T extends GameResult> {

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
    @Field("game_result")
    @JsonProperty("game_result")
    private T gameResult;

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
    public T getGameResult() {
        return gameResult;
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

    public void setGameResult(T gameResult) {
        this.gameResult = gameResult;
    }

    @Override
    public String toString() {
        return "ReferenceGame{" +
            "id=" + id +
            ", img='" + img + "'" +
            ", game_mode='" + gameMode + "'" +
            ", game_result='" + gameResult + "'" +
            '}';
    }
}
