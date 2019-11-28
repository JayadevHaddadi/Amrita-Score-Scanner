package edu.amrita.aerl.scorereader.core;

/**
 * Made at Amrita E-learning Research Lab
 *
 * Created by mreza on 19-Jan-18.
 */

public class SheetConfigNew2 extends SheetConfig{
    final int totalWidth = 18500;
    final int totalHeight = 10200;
    public final int[][] layout = {
        {L, L, S, L, L, S, L, D, L, L, L, D, D, D, D, D, D},
        {D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, T, T},
        {D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, T, T},
        {D, D, D, D, D, D, D, D, D, D, D, D, D, D, D, T, T},
        {S, S, S, S, S, S, S, S, S, S, S, S, S, S, G, G, G}};

    final int boxWidth = 1040;
    final int boxHeight = 1400;

    final double startRollY = 1265;
    final double startDigitX = 605;
    final double startDigitY = 3665;

    final double boxToBoxX = 1083;
    final int boxToBoxY = 1920;
}
