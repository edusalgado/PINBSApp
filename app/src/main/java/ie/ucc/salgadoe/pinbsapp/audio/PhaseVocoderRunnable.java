package ie.ucc.salgadoe.pinbsapp.audio;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import ie.ucc.salgadoe.pinbsapp.data.Vector;

/**
 * Created by SalgadoE on 25/04/2018.
 */

public class PhaseVocoderRunnable implements Runnable {

    // Initialise variables here instead of on StethoscopeFragment
    private int mPerformance = 128;  // fijado a 33 por jonatan
    private static final int TEST_POOLSIZE = 17;
    private double [] chunk;
    private int chunkLenght;
    private int inputSamplingRate = 32;
    private int outputSamplingRate = 16000;
    private double mVocoderRate = inputSamplingRate / (double) outputSamplingRate;
    private int fftSize = 32;
    private int hop = 4;
    private Vector mSignal, mTransformedSignal;
    //private byte[] mSignalbytes;
    private ArrayList<Double> vocoder_samples = new ArrayList<>();


    /* Configure a vocoder*/
    PhaseVocoder vocoder = new PhaseVocoder(mVocoderRate, fftSize, hop);



    public void run(){
        mSignal = new Vector(chunk);
        mTransformedSignal = vocoder.transform(mSignal,false);
        double[] aux = mTransformedSignal.getDoubles();
        for(int i = 0; i< aux.length; i++){
            vocoder_samples.add(aux[i]);
        }

    }

    public void configure(){
        vocoder.setThreadPoolSize(TEST_POOLSIZE);
        chunk = new double[vocoder.getFftSize() + vocoder.getHop() * (mPerformance -1)];
        chunkLenght = chunk.length;


    }

    public void initialiseVocoder(double[] first){
        vocoder.initialise(new Vector(first),mPerformance);
    }

    public void readNextChunk(double[] input){

        //Tener en cuenta si input length es diferente a chunk length
        chunk = input;

    }
    public int getChunkLength(){
        return chunkLenght;
    }

    public int getHop(){
        return hop;
    }

    public ArrayList<Double> getVocoderSamples(){return vocoder_samples;}




}
