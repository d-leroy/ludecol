package com.irisa.ludecol.web.rest.dto;

import com.irisa.ludecol.domain.subdomain.GameMode;
import com.irisa.ludecol.domain.subdomain.ImageStatus;

import java.util.List;
import java.util.Map;

/**
 * Created by dorian on 06/07/15.
 */
public class ImagesStatisticsDTO {

    private List<GameModeStatistics> gameModeStatistics;

    public List<GameModeStatistics> getGameModeStatistics() {
        return gameModeStatistics;
    }

    public void setGameModeStatistics(List<GameModeStatistics> gameModeStatistics) {
        this.gameModeStatistics = gameModeStatistics;
    }

    @Override
    public String toString() {
        return "ImagesStatisticsDTO{" +
            "gameModeStatistics=" + gameModeStatistics +
            '}';
    }

    public static class GameModeStatistics {

        private GameMode gameMode;

        private Map<ImageStatus,Integer> map;

        public GameModeStatistics(GameMode gameMode, Map map) {
            this.gameMode = gameMode;
            this.map = map;
        }

        public GameMode getGameMode() {
            return gameMode;
        }

        public void setGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
        }

        public Map<ImageStatus, Integer> getMap() {
            return map;
        }

        public void setMap(Map<ImageStatus, Integer> map) {
            this.map = map;
        }

        @Override
        public String toString() {
            return "GameModeStatistics{" +
                "gameMode=" + gameMode +
                ", map=" + map +
                '}';
        }
    }
}
