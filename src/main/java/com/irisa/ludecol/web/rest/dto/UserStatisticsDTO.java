package com.irisa.ludecol.web.rest.dto;

import com.irisa.ludecol.domain.subdomain.GameMode;

import java.util.List;

/**
 * Created by dorian on 06/07/15.
 */
public class UserStatisticsDTO {

    private List<GameModeStatistics> gameModeStatistics;

    private Integer bonusPoints;

    private Integer totalEarnedPoints;

    private Integer minRank;

    private Float meanRank;

    public List<GameModeStatistics> getGameModeStatisticsDTOs() {
        return gameModeStatistics;
    }

    public void setGameModeStatistics(List<GameModeStatistics> gameModeStatistics) {
        this.gameModeStatistics = gameModeStatistics;
    }

    public Integer getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(Integer bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public Integer getTotalEarnedPoints() {
        return totalEarnedPoints;
    }

    public void setTotalEarnedPoints(Integer totalEarnedPoints) {
        this.totalEarnedPoints = totalEarnedPoints;
    }

    public Integer getMinRank() {
        return minRank;
    }

    public void setMinRank(Integer minRank) {
        this.minRank = minRank;
    }

    public Float getMeanRank() {
        return meanRank;
    }

    public void setMeanRank(Float meanRank) {
        this.meanRank = meanRank;
    }

    @Override
    public String toString() {
        return "UserStatisticsDTO{" +
            "gameModeStatistics=" + gameModeStatistics +
            ", bonusPoints=" + bonusPoints +
            ", totalEarnedPoints=" + totalEarnedPoints +
            ", minRank=" + minRank +
            ", meanRank=" + meanRank +
            '}';
    }

    public static class GameModeStatistics {

        private GameMode gameMode;
        private int averageScore;
        private int numberOfGames;

        public GameModeStatistics(GameMode gameMode, int averageScore, int numberOfGames) {
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
            return "GameModeStatistics{" +
                "gameMode=" + gameMode +
                ", averageScore=" + averageScore +
                ", numberOfGames=" + numberOfGames +
                '}';
        }
    }
}
