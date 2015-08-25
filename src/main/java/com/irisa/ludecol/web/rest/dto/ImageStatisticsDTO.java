package com.irisa.ludecol.web.rest.dto;

import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageStatus;

import java.util.List;

/**
 * Created by dorian on 06/07/15.
 */
public class ImageStatisticsDTO {

    private List<GameModeStatistics> gameModeStatistics;

    public List<GameModeStatistics> getGameModeStatistics() {
        return gameModeStatistics;
    }

    public void setGameModeStatistics(List<GameModeStatistics> gameModeStatistics) {
        this.gameModeStatistics = gameModeStatistics;
    }

    @Override
    public String toString() {
        return "ImageStatisticsDTO{" +
            "gameModeStatistics=" + gameModeStatistics +
            '}';
    }

    public static class GameModeStatistics {

        private GameMode gameMode;
        private int numberOfGames;
        private ImageStatus status;

        public GameModeStatistics(GameMode gameMode, int numberOfGames, ImageStatus status) {
            this.gameMode = gameMode;
            this.numberOfGames = numberOfGames;
            this.status = status;
        }

        public GameMode getGameMode() {
            return gameMode;
        }

        public void setGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
        }

        public int getNumberOfGames() {
            return numberOfGames;
        }

        public void setNumberOfGames(int numberOfGames) {
            this.numberOfGames = numberOfGames;
        }

        public ImageStatus getStatus() {
            return status;
        }

        public void setStatus(ImageStatus status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "GameModeStatistics{" +
                "gameMode=" + gameMode +
                ", numberOfGames=" + numberOfGames +
                ", status=" + status +
                '}';
        }
    }
}
