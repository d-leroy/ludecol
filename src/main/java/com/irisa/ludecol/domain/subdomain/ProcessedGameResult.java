package com.irisa.ludecol.domain.subdomain;

/**
 * Created by dorian on 22/05/15.
 */
public abstract class ProcessedGameResult extends GameResult {

    private int nbResults;

    public int getNbResults() {
        return nbResults;
    }

    public void setNbResults(int nbResults) {
        this.nbResults = nbResults;
    }

}
