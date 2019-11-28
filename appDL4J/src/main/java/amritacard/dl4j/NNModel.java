package amritacard.dl4j;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.InputStream;


public class NNModel {
    private static MultiLayerNetwork modelDigits;
    private static MultiLayerNetwork modelLetters;

    public static boolean initate(InputStream digitFile, InputStream letterFile) {
        try {
            modelDigits = ModelSerializer.restoreMultiLayerNetwork(digitFile);
            modelLetters = ModelSerializer.restoreMultiLayerNetwork(letterFile);
            Printer.p(modelDigits.summary());
            Printer.p(modelLetters.summary());
            return true;
        } catch (Exception e) {
            Printer.p("Cant load models, Abort");
            Printer.p(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static double[] forwardNNLetter(double[] trans) {
        return forwardNN(trans, modelLetters);
    }

    public static double[] forwardNNDigit(double[] trans) {
        return forwardNN(trans, modelDigits);
    }

    private static double[] forwardNN(double[] trans, MultiLayerNetwork model) {
        INDArray nd = Nd4j.create(trans);
//        nd = nd.reshape('c', new int[]{1, 1, 28, 28});
//        Printer.p("Printing box");
//        for (int y = 0; y < 28; y++) {
//            for (int x = 0; x < 28; x++) {
//                Printer.po(trans[x + y * 28]);
//            }
//            Printer.p();
//        }
//
//        Printer.p("" + modelLetters);

        INDArray results = model.output(nd);
        double[] res = new double[results.size(1)];
        for (int i = 0; i < results.size(1); i++)
            res[i] = results.getDouble(i);
        return res;
    }
//
//    public double[] forwardNN(double[] trans) {
//        // for(int i = 0 ; i<trans.length; i++){
//        //     if(i%28==0)
//        //         Printer.p();
//        //     Printer.po((int) trans[i]+" ");
//        // }
//        // Printer.p();
//        INDArray nd = Nd4j.create(trans);
////        Printer.p(nd.shapeInfoToString());
//
////        nd = nd.reshape('c', new int[]{1, 1, 28, 28});
////        Printer.p(nd.shapeInfoToString());
//
//        INDArray results = modelDigits.output(nd);
//        double[] res = new double[results.size(1)];
//        for (int i = 0; i < results.size(1); i++)
//            res[i] = results.getDouble(i);
//        return res;
//    }

}
