package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.irisa.ludecol.domain.subdomain.GameMode;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dorian on 01/07/15.
 */

@Document(collection = "T_OBJECTIVE")
public class Objective {

    @Id
    private String id;

    @Indexed
    @NotNull
    @JsonIgnore
    private String usr;

    @Field("game_mode")
    @JsonProperty("game_mode")
    private GameMode gameMode;

    @Field("games_to_complete")
    @JsonProperty("games_to_complete")
    private Integer nbGamesToComplete = 0;

    @Field("completed_games")
    @JsonProperty("completed_games")
    private Integer nbCompletedGames = 0;

    @Field("pending_games")
    @JsonProperty("pending_games")
    private List<String> pendingGames = new ArrayList<>();

    @Field("bonus_points")
    @JsonProperty("bonus_points")
    private Integer bonusPoints = 0;

    @Field("creation_date")
    @JsonProperty("creation_date")
    private DateTime creationDate;

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Integer getNbGamesToComplete() {
        return nbGamesToComplete;
    }

    public void setNbGamesToComplete(Integer nbGamesToComplete) {
        this.nbGamesToComplete = nbGamesToComplete;
    }

    public List<String> getPendingGames() {
        return pendingGames;
    }

    public void setPendingGames(List<String> pendingGames) {
        this.pendingGames = pendingGames;
    }

    public Integer getNbCompletedGames() {
        return nbCompletedGames;
    }

    public void setNbCompletedGames(Integer nbCompletedGames) {
        this.nbCompletedGames = nbCompletedGames;
    }

    public Integer getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(Integer bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Objective{" +
            "id='" + id + '\'' +
            ", usr='" + usr + '\'' +
            ", gameMode=" + gameMode +
            ", nbGamesToComplete=" + nbGamesToComplete +
            ", nbCompletedGames=" + nbCompletedGames +
            ", pendingGames=" + pendingGames +
            ", bonusPoints=" + bonusPoints +
            ", creationDate=" + creationDate +
            '}';
    }
}
