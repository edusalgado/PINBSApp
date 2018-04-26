package ie.ucc.salgadoe.pinbsapp.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

import ie.ucc.salgadoe.pinbsapp.data.DataConverter;

/**
 * Created by SalgadoE on 25/04/2018.
 */

public class VocoderPlayer {

    final int bufferPlayer = 2;// buffer Audio-player [seconds] --> no tengo nidea pa que sirve
    private AudioTrack mTrack;
    PhaseVocoderRunnable mVocoder = new PhaseVocoderRunnable();
    ArrayList<Double> samplesList = new ArrayList<>();

    public void configurePlayer(int outputSamplingRate){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int minBuffSize = AudioTrack.getMinBufferSize(outputSamplingRate, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_FLOAT);
            mTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    outputSamplingRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT,
                    minBuffSize * bufferPlayer,
                    AudioTrack.MODE_STREAM);
        }
        else{
            int minBuffSize = AudioTrack.getMinBufferSize(outputSamplingRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
            mTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    outputSamplingRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_8BIT,
                    minBuffSize * bufferPlayer, //number of seconds of the buffer given its sample rate
                    AudioTrack.MODE_STREAM);
        }

    }

    public void loadSamples(double sample){

        /*Process data */

        if(mTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT){
            /* Write data as PCM 8bits */
            mTrack.write(DataConverter.DoubleToByte(sample),0,1);

        } else if (mTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
            /* Write data as PCM 16bits */
            mTrack.write(DataConverter.DoubleToShort(sample), 0, 1);

        } else if (mTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_FLOAT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /* Write data as PCM 32 bit IEEE single precision float */
            mTrack.write(DataConverter.DoubleToFloat(sample), 0, 1, AudioTrack.WRITE_BLOCKING);

        } else {
            throw new ExceptionInInitializerError("The encoding must be 8bit, 16bit or float " +
                    "(API>=21), for the moment");
        }
    }
    public void runTest(){
        for(int i =0; i<samplesList.size();i++){
            loadSamples(samplesList.get(i));
        }
        playEEG();


    }
    public void playEEG(){
        mTrack.play();
    }
    public void pausePlayer(){
        mTrack.pause();
    }
    public void setSamples(ArrayList<Double> samples){ this.samplesList = samples;}
}
