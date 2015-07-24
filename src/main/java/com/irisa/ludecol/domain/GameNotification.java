package com.irisa.ludecol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;


/**
 * Created by dorian on 01/06/15.
 */

@Document(collection = "T_NOTIFICATION")
public class GameNotification {

    @Id
    private String id;

    @NotNull
    @JsonProperty("title")
    private String title;

    @NotNull
    @JsonProperty("content")
    private String content;

    @Indexed
    @NotNull
    @JsonIgnore
    private String usr;

    private String gameId;

    @JsonIgnore
    private Long timestamp;

    @JsonIgnore
    private boolean sent = false;

    @JsonIgnore
    private boolean viewed = false;


    public GameNotification() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public String toString() {
        return "Notification{" +
            "id=" + id +
            ", usr=" + usr +
            ", title='" + title + "'" +
            ", content='" + content + "'" +
            ", sent='" + sent + "'" +
            ", viewed='" + viewed + "'" +
            '}';
    }
}
