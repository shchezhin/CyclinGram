package ru.cyclingram.cyclingram;

public class Block {
    private String time;
    private String header;
    private String text;
    private float percent;

    public void setTime(String time) {
        this.time = time;
    }

    public String getHeader() {

        header = "This " + time;
        return header;
    }

    public String getText() {
        if(percent>=1){
            text = "This " + time + " I am " + String.format("%.2f", (100*percent)-100 ) + "% faster than last " + time + "!";
        } else {
            text = "This " + time + " I am " + String.format("%.2f", 100-(100*percent) ) + "% slower than last " + time + "...";
        }
        return text;
    }

    public String getTextForSplit() {
        if(percent>=1){
            text = "This " + time + " I am#" + String.format("%.2f", (100*percent)-100 ) + "% faster#than last " + time + "!";
        } else {
            text = "This " + time + " I am#" + String.format("%.2f", 100-(100*percent) ) + "% slower#than last " + time + "...";
        }
        return text;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

}
