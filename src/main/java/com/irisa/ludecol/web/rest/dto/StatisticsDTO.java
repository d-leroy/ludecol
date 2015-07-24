package com.irisa.ludecol.web.rest.dto;

import java.util.List;

/**
 * Created by dorian on 06/07/15.
 */
public class StatisticsDTO {

    private List<GameModeStatisticsDTO> gameModeStatisticsDTOs;

    private Integer bonusPoints;

    private Integer totalEarnedPoints;

    public List<GameModeStatisticsDTO> getGameModeStatisticsDTOs() {
        return gameModeStatisticsDTOs;
    }

    public void setGameModeStatisticsDTOs(List<GameModeStatisticsDTO> gameModeStatisticsDTOs) {
        this.gameModeStatisticsDTOs = gameModeStatisticsDTOs;
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

    @Override
    public String toString() {
        return "StatisticsDTO{" +
            "gameModeStatisticsDTOs=" + gameModeStatisticsDTOs +
            ", bonusPoints=" + bonusPoints +
            ", totalEarnedPoints=" + totalEarnedPoints +
            '}';
    }
}
