package com.sparkscientist.beans;

/**
 * @author Anurag
 * @description 
 */
public class WordNCount {
    private String word;
    private Integer count;

    public WordNCount(String word, Integer count) {
        this.word = word;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return word + ":" + count;
    }
}

