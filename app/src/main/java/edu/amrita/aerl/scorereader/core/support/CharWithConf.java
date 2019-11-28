package edu.amrita.aerl.scorereader.core.support;

import edu.amrita.aerl.jayadev.util.Printer;

/**
 * Created by mreza on 08-Jan-18.
 */

public class CharWithConf {
    private char[] chars;
    private boolean isCorrected = false;
    private int correctIndex;
    private double highestConf;
    private double[] confs;

    public CharWithConf(double[] confs, char[] chars) {
        this.confs = confs;
        this.chars = chars;
        correctIndex = -1;
        highestConf = -1;
        for (int i = 0; i < confs.length; i++) {
            if (confs[i] > highestConf) {
                highestConf = confs[i];
                correctIndex = i;
            }
        }
    }

    public CharWithConf(char[] chars) {
        this.chars = chars;
        isCorrected = true;
    }

    public int getDigit() {
        return Integer.parseInt("" + chars[correctIndex]);
    }

    public char getChar() {
        return chars[correctIndex];
    }

    public String stringValue() {
        return String.valueOf(getChar());
    }

    public int getSecond() {
        double bestConf = 0, secondConf = 0;
        int bestIndex = -1, secondIndex = -1;
        for (int i = 0; i < confs.length; i++) {
            System.out.println(i + ", " + confs[i]);
            if (confs[i] > bestConf) {
                secondConf = bestConf;
                bestConf = confs[i];
                secondIndex = bestIndex;
                bestIndex = i;
            } else if (confs[i] > secondConf) {
                secondConf = confs[i];
                secondIndex = i;
            }
        }
        return secondIndex;
    }

    public double getConfidence() {
        return highestConf;
    }

    public int getProcent() {
        return (int) Math.round(highestConf*100);
    }


    public void setCorrectIndex(int correctIndex) {
        if(correctIndex == this.correctIndex)
            return;
        this.correctIndex = correctIndex;
        this.isCorrected = true;
    }

    @Override
    public String toString() {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < confs.length; i++) {
            temp.append(chars[i]).append(": ").append(Printer.decimals2(confs[i])).append("\n");
        }
        return temp.toString();
    }

    public boolean isCorrected() {
        return isCorrected;
    }
}
