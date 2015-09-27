package com.example.shubham.mapstest;

/**
 * Created by shubham on 27-09-2015.
 */
public class Duration {
    public Duration(String text, long value) {
        this.text = text;
        this.value = value;
    }

    private String text;
    private long value;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

}
