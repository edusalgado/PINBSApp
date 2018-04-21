package Utilities;

/**
 * Created by SalgadoE on 30/11/2017.
 */

public class Buffer {
    static int size;
    double [] buffer;
    int cont = 0;
    public Buffer(int size){
        this.size = size;
        buffer = new double[size];
    }
    public void addValue(double value){
        if (cont == size){
            for(int i =0, len =size-1; i<len; i++){
                buffer[i]= buffer[i+1];
            }
            buffer[size-1] = value;
        }else{
            buffer[cont] = value;
            cont ++;
        }


    }
    public double[] getValue(){
        return buffer;
    }

}
