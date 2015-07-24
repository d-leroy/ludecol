package com.irisa.ludecol.web.rest.dto;

import com.irisa.ludecol.domain.subdomain.GameMode;

/**
 * Created by dorian on 26/06/15.
 */
public class GameModeStatisticsDTO {

    private GameMode gameMode;
    private int averageScore;
    private int numberOfGames;

    public GameModeStatisticsDTO(GameMode gameMode, int averageScore, int numberOfGames) {
        this.gameMode = gameMode;
        this.averageScore = averageScore;
        this.numberOfGames = numberOfGames;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public int getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(int averageScore) {
        this.averageScore = averageScore;
    }

    public int getNumberOfGames() {
        return numberOfGames;
    }

    public void setNumberOfGames(int numberOfGames) {
        this.numberOfGames = numberOfGames;
    }

    @Override
    public String toString() {
        return "GameModeStatisticsDTO{" +
            "gameMode=" + gameMode +
            ", averageScore=" + averageScore +
            ", numberOfGames=" + numberOfGames +
            '}';
    }
}
