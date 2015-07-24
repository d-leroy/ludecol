package com.irisa.ludecol.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by dorian on 16/07/15.
 */
@Document(collection = "T_FEEDBACK")
public class Feedback {

    @Id
    private String id;

    private String title;

    private Integer rating;

    private String answer;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "Feedback{" +
            "title='" + title + '\'' +
            ", rating=" + rating +
            ", answer='" + answer + '\'' +
            '}';
    }
}
