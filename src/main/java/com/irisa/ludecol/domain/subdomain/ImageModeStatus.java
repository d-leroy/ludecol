package com.irisa.ludecol.domain.subdomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dorian on 24/08/15.
 */
public class ImageModeStatus {

    private GameMode mode;

    private Integer gameNumber = 0;

    public GameMode getMode() {
        return mode;
    }

    public ImageModeStatus() {}

    public ImageModeStatus(GameMode mode, Integer gameNumber) {
        this.mode = mode;
        this.gameNumber = gameNumber;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public Integer getGameNumber() {
        return gameNumber;
    }

    public void setGameNumber(Integer gameNumber) {
        this.gameNumber = gameNumber;
    }
}
