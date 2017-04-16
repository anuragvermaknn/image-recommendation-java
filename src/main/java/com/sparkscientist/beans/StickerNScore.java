package com.sparkscientist.beans;

/**
 * @author Anurag
 * @description 
 */
public class StickerNScore {

    private String stickerId;
    private double score;

    public StickerNScore(String stickerId, double score) {
        this.stickerId = stickerId;
        this.score = score;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "StickerNScore{" +
                "stickerId='" + stickerId + '\'' +
                ", score=" + score +
                '}';
    }
}
