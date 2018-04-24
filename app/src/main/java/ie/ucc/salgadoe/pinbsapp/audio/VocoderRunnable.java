package ie.ucc.salgadoe.pinbsapp.audio;

import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import ie.ucc.salgadoe.pinbsapp.data.MyRunnable;
import ie.ucc.salgadoe.pinbsapp.data.Vector;
import ie.ucc.salgadoe.pinbsapp.data.Pipe;


/**
 * Process the output data given a pipe.
 * <p>
 * The reading is blocking. The pipe will be released when the sink is closed and the pipe
 * completely empty.
 */
public class VocoderRunnable extends MyRunnable {
    private static final String TAG = "VocoderRunnable";
    //    private final Object lock = new Object();
    private byte[] mSample;
    private boolean ALLOW_SKIPPING = true;
    private boolean EOF_received;
    private boolean mStopFlag;
    //    private int mSamplesReaded;
    private int mSamplesProcessed;
    private int mSamplesSkipped;
    private int mPerformance;
    private long mLag, mAvgLag, mCounterLag;
    private double[] chunk;
    private Pipe mInputPipe, mOutputPipe;
//    private PipedInputStream mSource;
//    private PipedOutputStream mOutputSink;
    private DataInputStream mSource;
    private DataOutputStream mOutputSink;
    private PhaseVocoder mVocoder = null;
    private Vector mSignal, mTransformedSignal;
    private boolean runInMultithread = false;

    /**
     * Configure skipping samples when the output buffer is full. Default value: true
     *
     * @param skip true for allowing skip samples, false to do not allow and wait for free space
     *             in the buffer
     */
    public void setAllowSkipping(boolean skip) {
        ALLOW_SKIPPING = skip;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
//        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
//        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        SystemClock.sleep(super.delay);

        /* Initialisation */
        mSamplesProcessed = 0;
        mSamplesSkipped = 0;
        mLag = 0;
        mAvgLag = 0;
        mCounterLag = 0;
        EOF_received = false;
        mStopFlag = false;
        mSample = new byte[8];
//        mSamplesReaded = 0;

        try {
            double[] first = initialiseChunk();
            mVocoder.initialise(new Vector(first), mPerformance);
//            Complex64.generateTrigonometricLUT(1000);
            while (!mStopFlag) {
                long startingTime = System.nanoTime();
                /* Note: the order of the following conditionals is important */
                if (EOF_received) {
                    Log.d(TAG, "run: EOF received, stopping");
                    mStopFlag = true;
                } else if (mOutputPipe.isFull() & ALLOW_SKIPPING) {
                    skipAChunk();
                } else {
                    doWork();
//                    simulateWorking();
                }
                mLag = System.nanoTime() - startingTime;
                mAvgLag += mLag;
                mCounterLag++;
            }

        } catch (NullPointerException e) {
            Log.d(TAG, "run: The vocoder or some of the pipes are not defined, ending");
            e.printStackTrace();
        } catch (EOFException expected) {
            Log.d(TAG, "run: EOF received, ending");
        } catch (IOException e) {
            Log.d(TAG, "run: Problem reading or writing the pipe");
        }

        stop();
        Log.d(TAG, "run: THREAD STOPPED");
    }


    @Override
    protected void doWork() throws EOFException, IOException {
        /* Get a chunk */
        readNextChunk();

        /* Process */
        mSignal = new Vector(chunk);
        mTransformedSignal = mVocoder.transform(mSignal, runInMultithread);

        /* Send result to the pipe, sample by sample */
//        for (int i = 0; i < mTransformedSignal.getLength(); i++) {
//            mOutputSink.write(DataConverter.DoubleToByteArray(mTransformedSignal.getValueOfIndex(i)));
//        }

        /* Send result to the pipe, alternative version which writes all the samples in a row */
        mOutputSink.write(mTransformedSignal.getBytes());
//        mOutputSink.write(mTransformedSignal.get16MSB());
//        mOutputSink.write(mTransformedSignal.get16LSB());


        /* Send result to a pipe (test) */
//        byte[] byteArray;
//        Double f = 1000.0;  //corresponds to a tone at 4Hz in EEG
//        for (int i = 0; i < mTransformedSignal.getLength(); i++) {
//            Double mSignal = Math.cos(2 * Math.PI * f * i / 16000);
//            byteArray = DataConverter.DoubleToByteArray(mSignal);
//            mOutputSink.write(byteArray);
//        }
        mSamplesProcessed += mTransformedSignal.getLength();
        /* Notifies the reader that there are new samples (if not it will wait for 1 second) */
        mOutputSink.flush();
    }

    /**
     * Gets a sample from the source of a pipe
     *
     * @return the readed sample
     * @throws IOException if EOF is received or there is a problem reading from the pipe
     */
    private Double getASample() throws EOFException, IOException {
        return mSource.readDouble();
    }

//    /**
//     * Gets a sample from the source of a pipe
//     *
//     * @return the readed sample
//     * @throws IOException if EOF is received or there is a problem reading from the pipe
//     */
//    private Double getASample() throws EOFException, IOException {
//        int bytesReaded = mSource.read(mSample);
//        Double d = DataConverter.ByteArrayToDouble(mSample);
//
//        if (bytesReaded == -1) {
//            throw new EOFException();
//        } else {
////            mSamplesReaded++;
//            return d;
//        }
//    }
//
    /**
     * Fills the whole buffer (chunk) with samples.
     *
     * @throws EOFException         see {@link #getASample() getASample} method
     * @throws IOException          see {@link #getASample() getASample} method
     * @throws NullPointerException see {@link System#arraycopy(Object, int, Object, int, int) System.arraycopy} method
     */
    private double[] initialiseChunk() throws EOFException, IOException, NullPointerException {
        /* It should not matter, but just in case (it is called only once) */
        for (int i = 0; i < mVocoder.getHop(); i++) {
            chunk[i] = 0;
        }
        for (int i = mVocoder.getHop(); i < chunk.length; i++) {
            chunk[i] = getASample();
        }
        double[] first = new double[mVocoder.getFftSize()];
        System.arraycopy(chunk, mVocoder.getHop(), first, 0, first.length);
        return first;
    }

    /**
     * Prepares the next chunk getting some new samples (depends on the vocoder newData) and
     * push them at the end of the chunk, discarding some of the oldest samples. <--[·······]<--
     * <p>
     * Note: Do not confuse this sample-keeping with the istft'mSignal one. Here the last window,
     * that is the last frame, is not inserted in the final stream (only used for interpolation) so
     * we need it entirely in the next processing chunk.
     *
     * @throws EOFException see {@link #getASample() getASample} method
     * @throws IOException  see {@link #getASample() getASample} method
     */
    private void readNextChunk() throws EOFException, IOException {
        System.arraycopy(chunk, chunk.length - mVocoder.getFftSize() - 1, chunk, 0, mVocoder.getFftSize());
//        for (int i = chunk.length - mVocoder.getFftSize(); i < chunk.length; i++) {
        for (int i = mVocoder.getFftSize(); i < chunk.length; i++) {
            chunk[i] = getASample();
        }
    }

    /**
     * Skip samples (reading from the buffer but not using them)
     *
     * @throws EOFException see {@link #getASample() getASample} method
     * @throws IOException  see {@link #getASample() getASample} method
     */
    private void skipAChunk() throws EOFException, IOException {
//        if (mSamplesSkipped % 16000 == 0) {
            /* FIXME: uncomment log */
//            Log.d(TAG, "skipAChunk: Pipe2 full, skipping samples : " + mSamplesSkipped);
//        }
        /* TODO: replace to getASample ? */
        readNextChunk();
        mSamplesSkipped += chunk.length - mVocoder.getFftSize();
    }

    /**
     * Returns the number of processed samples in thousands.
     *
     * @return samples processed
     */
    public int getNumberOfSamplesProcessed() {
        return mSamplesProcessed / 1000;
    }

    /**
     * Returns the number of skipped samples in thousands.
     *
     * @return samples skipped
     */
    public int getNumberOfSamplesSkipped() {
        return mSamplesSkipped / 1000;
    }

    public void setInputPipe(Pipe p) {
        mInputPipe = p;
        mSource = p.getSoure();
    }

    public void setOutputPipe(Pipe p) {
        mOutputPipe = p;
        mOutputSink = mOutputPipe.getSink();
    }

    /**
     * Returns the cadence of processing a sample
     *
     * @return samples per second
     */
    public int getLag() {
        try {
            Long hz = (long) (mTransformedSignal.getLength() * 1.0e9 / mLag);
            return hz.intValue();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    /**
     * Returns the cadence of processing a sample in average
     *
     * @return samples per second
     */
    public int getmAvgLag() {
        try {
//            Long hz = (long) (mTransformedSignal.getLength() * 1.0e9 / mLag);
            Long hz = (long) (mTransformedSignal.getLength() * 1.0e9 / (mAvgLag / (double) mCounterLag));
            return hz.intValue();
        } catch (NullPointerException | ArithmeticException e) {
            return 0;
        }
    }

    /**
     * Sets the vocoder to be used and a parameter used to balance the cpu and memory cost.
     *
     * @param vocoder     a configured PhaseVocoder
     * @param performance determines the number of vectors used to interpolate at the same loop,
     *                    it does not affect at the result output, only the performance in terms of
     *                    memory usage and memory overhead. It cannot be less than 2.
     *                    //     * @param chunkSize : the size of the chunk used audioRunnable a input of the vocoder, it has be with the number of samples by:  nSamples = fftSize + newData * chunkSize
     */
    public void setVocoder(PhaseVocoder vocoder, int performance) {
        mVocoder = vocoder;
        if (performance < 2) {
            throw new ExceptionInInitializerError("performance paramter cannot be less than 2");
        } else mPerformance = performance;

        chunk = new double[mVocoder.getFftSize() + mVocoder.getHop() * (performance - 1)];
        Log.d(TAG, "setVocoder: set chunk length of " + chunk.length + " samples.");
    }

    /**
     * Dev method. Switches between single-thread and multi-thread mode on the fly
     *
     * @param activate : set true to work in multiple threads, false to be in a single thread
     */
    public void setMultithreadedMode(boolean activate) {
        runInMultithread = activate;
    }

    /**
     * Closes the associated streams and release associated resources to the thread
     */
    @Override
    public void stop() {
        try {
            mSource.close();
            mInputPipe.release();
        } catch (IOException e) {
            Log.d(TAG, "stop: Problem stopping");
            e.printStackTrace();
        }
    }

}

//    private void nextChunk() throws IOException {
//        int leaveOldNSamples = mVocoder.getFftSize();// - mVocoder.getHop();
//        System.arraycopy(chunk, chunk.length - leaveOldNSamples, chunk, 0, leaveOldNSamples);
//        for (int i = leaveOldNSamples; i < chunk.length; i++) {
//            chunk[i] = getSample();
//        }
//    }

//    public void setVocoder(PhaseVocoder vocoder, int chunkSize) {
//    /** only 4 new samples*/
//    private void nextChunk() throws IOException {
//        System.arraycopy(chunk, mVocoder.getHop(), chunk, 0, chunk.length - mVocoder.getHop());
//        for (int i = chunk.length - mVocoder.getHop(); i < chunk.length; i++) {
//            chunk[i] = getSample();
//        }
//    }

//    private void simulateWorking() throws IOException {
//        Vector v;
//        nextChunk();
//        for (int i = 0; i < v.getLength(); i++) {
//            mOutputSink.write(DataConverter.DoubleToByteArray(v.getValueOfIndex(i)));
//        }
//        mSamplesProcessed += v.getLength();
//    }
