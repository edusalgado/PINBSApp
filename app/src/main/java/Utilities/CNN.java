package Utilities;


import java.util.ArrayList;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;


/**
 * Created by SalgadoE on 02/11/2017.
 */

public class CNN {

    ArrayList<Double> mymean = new ArrayList<>();
    double output;
    int startx;
    double range;
    int average;


    public static double[][] Conv1D(double[][] input, double[][] weights, boolean relu, boolean use_bias, double[] bias) {

        int input_row_len = input.length;
        int input_col_len = input[0].length;
        int weights_row_len = weights.length;
        int weights_col_len = weights[0].length;
        int sizeloop = weights_row_len / input_col_len;

        double[][] salida = new double[input_row_len - (weights_col_len - 1)][weights_row_len / input_col_len];
        int len_salida = salida.length;
        double value;
        for (int i = 0; i < sizeloop; i++) {
            for (int j = 0; j < len_salida; j++) {
                value = 0;
                for (int k = 0; k < input_col_len; k++) {
                    for (int l = 0; l < weights_col_len; l++) {

                        value += input[j + l][k] * (weights[k + (i * input_col_len)][weights_col_len - (l + 1)]);
                    }
                }
                salida[j][i] = value;
                if (use_bias == true) {
                    salida[j][i] = value + bias[i];
                }
                if (relu == true) {
                    if (salida[j][i] < 0) {
                        salida[j][i] = 0;
                    }
                }

            }

        }

        return salida;
    }

    public static double[][] AveragePooling1D(double[][] layer, int pool_size, int strides) {

        double[][] pooling = new double[(int) (((double) (layer.length - (pool_size - 1)) / strides) + 0.5)][layer[0].length];
        double value;
        for (int i = 0, len = layer[0].length; i < len; i++) {
            for (int j = 0, len1 = pooling.length; j < len1; j++) {
                value = 0;
                for (int k = 0; k < pool_size; k++) {
                    value += (layer[(strides * j) + k][i]);
                }
                value /= pool_size;
                pooling[j][i] = value;
            }
        }
        return pooling;
    }

    public static double[][] BatchNormalization(double[][] layer, double[][] parameters) { // Beta, Gamma, Moving_mean, Moving_variance
        double[][] output = new double[layer.length][layer[0].length];
        double epsilon = 1E-3;
        double denominator;
        for (int i = 0, len = layer[0].length; i < len; i++) {
            denominator = parameters[3][i] + epsilon;
            denominator = sqrt(denominator);

            for (int j = 0, len1 = layer.length; j < len1; j++) {
                layer[j][i] -= parameters[2][i];
                output[j][i] = layer[j][i] / denominator;
                output[j][i] *= parameters[1][i] ;
                output[j][i] += parameters[0][i];

            }
        }
        return output;
    }

    public static double[] GlobalAveragePooling1D(double[][] layer) {

        double[] average = new double[layer[0].length];
        for (int i = 0, len = layer[0].length; i < len; i++) {
            average[i] = 0;
            for (int j = 0, len1 = layer.length; j < len1; j++) {
                average[i] += layer[j][i];
            }
            average[i] /= layer.length;
        }
        return average;
    }

    public static double[] SoftmaxActivation(double[] layer) {

        double[] softmax = new double[layer.length];
        double value = 0;
        for (int i = 0, len = layer.length; i < len; i++) {
            softmax[i] = exp(layer[i]);
            value += softmax[i];
        }
        for (int i = 0, len = layer.length; i < len; i++) {
            softmax[i] /= value;
        }
        return softmax;
    }
    public static double MAF(Buffer input){
        double[] buffer = input.getValue();
        double value = 0;
        int len  = buffer.length;
        for(int i = 0; i<len; i++){
            value += buffer[i];
        }
        value = value/len;

        return value;
    }

    public double RespirationAdaptation(int kk, ArrayList<Integer> idxtmp, ArrayList<Double> input){
        double aux;
        range = 0;
        average = 0;
        if(kk > 0 && (idxtmp.get(kk)-idxtmp.get(kk-1)) == 1){
            mymean.add(kk,mymean.get(kk-1));
        } else{
            startx = idxtmp.get(kk) - 5760;
            if(startx<0){
                startx = 0;
            }
            int len = idxtmp.get(kk);
            for(int i = startx; i < len; i++){
                aux = input.get(i);
                if (aux < 0.5){
                    range = range + aux;
                    average++;
                }
            }
            if(range == 0){
                mymean.add(kk,0.0);
            } else{
                mymean.add(kk,range/average);
            }
        }

        return output = input.get(idxtmp.get(kk))-mymean.get(kk);
    }
}


