package com.irisa.ludecol.web.rest.dto;

import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageStatus;

import java.util.EnumMap;
import java.util.List;

/**
 * Created by dorian on 06/07/15.
 */
public class ImageSetStatisticsDTO {

    private List<GameModeStatistics> gameModeStatistics;

    public List<GameModeStatistics> getGameModeStatistics() {
        return gameModeStatistics;
    }

    public void setGameModeStatistics(List<GameModeStatistics> gameModeStatistics) {
        this.gameModeStatistics = gameModeStatistics;
    }

    @Override
    public String toString() {
        return "ImageSetStatisticsDTO{" +
            "gameModeStatistics=" + gameModeStatistics +
            '}';
    }

    public static class GameModeStatistics {

        private GameMode gameMode;

        private EnumMap<ImageStatus,Integer> enumMap;

        public GameModeStatistics(GameMode gameMode, EnumMap enumMap) {
            this.gameMode = gameMode;
            this.enumMap = enumMap;
        }

        public GameMode getGameMode() {
            return gameMode;
        }

        public void setGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
        }

        public EnumMap<ImageStatus, Integer> getEnumMap() {
            return enumMap;
        }

        public void setEnumMap(EnumMap<ImageStatus, Integer> enumMap) {
            this.enumMap = enumMap;
        }

        @Override
        public String toString() {
            return "GameModeStatistics{" +
                "gameMode=" + gameMode +
                ", enumMap=" + enumMap +
                '}';
        }
    }
}
