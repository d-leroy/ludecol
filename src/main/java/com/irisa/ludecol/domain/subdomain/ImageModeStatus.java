package com.irisa.ludecol.domain.subdomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dorian on 24/08/15.
 */
public class ImageModeStatus {

    private ImageStatus status = ImageStatus.UNAVAILABLE;

    private Integer gameNumber = 0;

    public ImageModeStatus() {}

    public ImageModeStatus(ImageStatus status, Integer gameNumber) {
        this.status = status;
        this.gameNumber = gameNumber;
    }

    public ImageStatus getStatus() {
        return status;
    }

    public void setStatus(ImageStatus status) {
        this.status = status;
    }

    public Integer getGameNumber() {
        return gameNumber;
    }

    public void setGameNumber(Integer gameNumber) {
        this.gameNumber = gameNumber;
    }
}
