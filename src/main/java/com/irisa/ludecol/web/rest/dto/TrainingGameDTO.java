package com.irisa.ludecol.web.rest.dto;

/**
 * Created by dorian on 12/05/15.
 */
public abstract class TrainingGameDTO {

    private String id;

    private String img;

    private int score;

    private boolean completed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
