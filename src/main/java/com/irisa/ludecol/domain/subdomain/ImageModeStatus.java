package com.irisa.ludecol.domain.subdomain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.*;

/**
 * Created by dorian on 24/08/15.
 */
public class ImageModeStatus {

    private ImageStatus status = ImageStatus.UNAVAILABLE;

    private Integer gameNumber = 0;

    @JsonIgnore
    private List<GameResult> gameResults = new ArrayList<>();

    @JsonIgnore
    private Map referenceResult;

    public ImageModeStatus() {}

    public ImageModeStatus(ImageStatus status) {
        this.status = status;
    }

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

    public List<GameResult> getGameResults() {
        return gameResults;
    }

    public Map getReferenceResult() {
        return referenceResult;
    }

    public void setReferenceResult(Map referenceResult) {
        this.referenceResult = referenceResult;
    }

    @Override
    public String toString() {
        return "ImageModeStatus{" +
            "status=" + status +
            ", gameNumber=" + gameNumber +
            ", gameResults=" + gameResults +
            ", referenceResult=" + referenceResult +
            '}';
    }
}
