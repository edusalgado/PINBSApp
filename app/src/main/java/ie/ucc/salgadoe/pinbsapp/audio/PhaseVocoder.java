package ie.ucc.salgadoe.pinbsapp.audio;

import java.util.ArrayList;

import ie.ucc.salgadoe.pinbsapp.data.Complex64;
import ie.ucc.salgadoe.pinbsapp.data.Complex64Vector;
import ie.ucc.salgadoe.pinbsapp.data.Complex64Matrix;
import ie.ucc.salgadoe.pinbsapp.data.Vector;
import ie.ucc.salgadoe.pinbsapp.data.Window;



/**
 * Defines a Phase Vocoder.
 * <p>
 * Time-scales a signal mScaleRate times faster, calculates an overlapped STFT with (mFtSize/mHop)%,
 * squeezes it by a factor of mScaleRate and does the inverse spectrogram.
 */
public class PhaseVocoder {
    private static String TAG = "PhaseVocoder";
    private int mFftSize;       // <--> n
    private int mHop;           // is the STFT hop size, defaults to N/2, where N is the FFT size
    private double mScaleRate;  // <--> r
    private double[] mAcumulatedPhase;
    private Window mWindowStft, mWindowIstft;
    private Vector mTail = null;
    private double[] mDeltaPhi;
    private Complex64Matrix mInterpolatedMatrix;
    private Complex64Matrix mFrames;
    private InterpolateMultithread interpool = new InterpolateMultithread(Runtime.getRuntime().availableProcessors());

    /* NOTE: performance */
    private double[] magnitudeResult;
    private Complex64[] frameResult;


    /* Constructors */
    PhaseVocoder(double scaleRate, int FftSize) {
        this(scaleRate, FftSize, FftSize / 4);
    }

    PhaseVocoder(double scaleRate) {
        this(scaleRate, 1024, 1024 / 4);
    }

    /**
     * Sets the properties of the vocoder
     *
     * @param scaleRate scale rate
     * @param fftSize   number of points of the FFT
     * @param hop       the number of samples to shift
     */
    public PhaseVocoder(double scaleRate, int fftSize, int hop) {
        if (scaleRate == 0) {
            throw new IllegalArgumentException("The scaleRate must be positive and greater than 0.");
        }
        mScaleRate = Math.abs(scaleRate);
        mFftSize = Math.abs(fftSize);
        mHop = Math.abs(hop);
//        Complex64.generateTrigonometricLUT(1000000);


    }

    /* Methods */

    /**
     * Sets the number of threads available in the pool when running the multi-thread method. If
     * not set, the default value is {@link Runtime#availableProcessors()}
     *
     * @param poolSize : number of threads in the pool
     */
    public void setThreadPoolSize(int poolSize) {
        if (poolSize > 0) {
            interpool = new InterpolateMultithread(poolSize);
        } else throw new IllegalArgumentException("The pool size cannot be negative!");
    }

    /**
     * Initialise a Phase Vocoder
     *
     * @param signal      first chunk.
     * @param performance how many frames use in interpolation
     */
    void initialise(Vector signal, int performance) {
        /* Performance */
        magnitudeResult = new double[mFftSize/2+1];
        frameResult = new Complex64[mFftSize/2+1];
        /* Performance */

        /* Define a window */
        mWindowStft = new Window(Window.WINDOW_HANN, mFftSize);
        /* Make stft-istft loop be identity for 25% hop (if using hann windowing) */
        mWindowIstft = mWindowStft.clone();
        mWindowIstft.scale(2.0 / 3.0);

        if (signal.getLength() < mFftSize) {
            throw new IllegalArgumentException("signal length cannot be less than a window");
        }
        /* Compute first STFT frame */
        Complex64Vector firstFrame = signal.stft(mFftSize, mWindowStft, mHop).getVector(0);

        /* Configure and initialise some variables */
//        int nVectors = (signal.getLength() - mFftSize) / mHop * ((int) (1.0 / mScaleRate)) + 1;
//        int nVectors = (performance - 1) * (int) (1.0 / mScaleRate) + 1;
        int nVectors = (performance - 1) * (int) (1.0 / mScaleRate) + 1;
        int nBands = firstFrame.getLength();
        mInterpolatedMatrix = new Complex64Matrix(new Complex64(0.0, 0.0), nVectors, nBands);

        int N = 2 * (nBands - 1); //N bands (originally)
        if (mHop == 0) mHop = N / 2;

        /* Expected phase advance in each bin */
        mDeltaPhi = new double[nBands];
        for (int i = 0; i <= N / 2; i++) {
            mDeltaPhi[i] = 2.0 * Math.PI * mHop * i / N; //double value = (2 * Math.PI * mHop) * i / N;
        }

        /* Phase accumulator. Preset to expectedPhaseChange of first frame for perfect reconstruction in case
        of 1:1 time scaling
        */
        mAcumulatedPhase = firstFrame.getArguments();

        /* Set first output audio samples */
        mTail = new Vector(0.0, mFftSize);

        /* Define new spectrogram's size */
        mFrames = new Complex64Matrix(performance, mFftSize / 2 + 1);
    }

    /**
     * TODO: check performance using pvsample with more than two vectors (to reduce memory reservation)
     * <p>
     * Applies the vocoder to a given Vector. Stateless.
     *
     * @param signal the signal to be transformed
     * @return the result signal of the transform
     */
    public Vector transform(Vector signal, boolean multithreaded) {
        /* With hann windowing on both input and output, we need 25% window overlap for smooth
         * reconstruction. Effect of hanns at both ends is a cumulated cos^2 window
         * (for r = 1 anyway); need to scale magnitudes by 2/3 for identity input/output
         * scf = 2/3.
         */
        if (mTail == null) throw new ExceptionInInitializerError("Please, initialise() first !");

        /* Calculate the basic STFT, magnitude scaled <--> X = stft(x', n, n, hop) */
        mFrames = signal.stft(mFftSize, mWindowStft, mHop);

        /* Interpolate vectors */
//        Log.d(TAG, "transform: Size inter " + interpolatedFrames.getNVectors() + " x " + interpolatedFrames.getNBands());
        Complex64Matrix interpolatedFrames = interpolate(mFrames, multithreaded);
//        Log.d(TAG, "transform: Size inter " + interpolatedFrames.getNVectors() + " x " + interpolatedFrames.getNBands());

        /* Invert to a waveform */
        Vector interpolatedSignal = mWindowIstft.istft(mFftSize, mHop, interpolatedFrames);

        /* Mix with the previous samples so they overlap and its like a continuous stream */
        Vector mixedSignal = interpolatedSignal.mix(mTail, 0);

        /* Remove from the stream the last samples and keep them to be mixed with future ones. */
        Vector cutSignal = mixedSignal.getRange(0, mixedSignal.getLength() - mTail.getLength() - 1);
        mTail = mixedSignal.getRange(mixedSignal.getLength() - mTail.getLength(), mixedSignal.getLength() - 1);

        /* Normalise to (-1, 1). With a normalised input, the original output is [-2, 2].
        Comment: the system is normalising twice, once at the input and another time at the
        output. In fact, its only need the later but this way you only have to normalise the
        input on hardware changes.
        */
        cutSignal.scale(1 / 2.01);

        /* Testing: Check max value */
//        double[] tap = new double[64];
//        System.arraycopy(s.getPrimitive(), 0, tap, 0, 64);
//        double max = 0, min = 0;
//        double[] ooo = outSignal.getPrimitive();
//        for (int i = 0; i < outSignal.getLength(); i++) {
//            if (ooo[i] > max) max = ooo[i];
//            if (ooo[i] < min) min = ooo[i];
//        }
//        if (max > 1 || min < -1)
//            Log.d(TAG, "transform: min , max: " + min + " , " + max);

        return cutSignal;
    }

    /**
     * Interpolate an STFT array according to the PhaseVocoder.
     * <p>
     * It computes the time-samples (t), which specifies a path through the time-base defined by
     * the columns of 'input'. For each value of t, the spectral magnitudes in the columns of
     * 'input' are mInterpolatedMatrix, and the mAcumulatedPhase difference between the successive columns of
     * 'input' is calculated; a new column is created in the output array that preserves this
     * per-step mAcumulatedPhase advance in each bin. 'hop' is needed to calculate the 'null' mAcumulatedPhase advance
     * expected in each bin.
     * <p>
     * Note: t is defined relative to a zero origin, so 0.1 is 90% of the first column of b, plus 10% of the second.
     *
     * @param input an STFT matrix with N/2+1 bands
     * @return the mInterpolatedMatrix matrix
     */
    Complex64Matrix interpolateFrames(Complex64Matrix input) {  //halfmode is a test to run only one loop and write 2 columns, delete it
//    Complex64Matrix interpolateFrames(Complex64Matrix input, boolean halfmode) {  //halfmode is a test to run only one loop and write 2 columns, delete it
        if (input.getNVectors() < 2) {
            throw new IllegalArgumentException("It needs at least 2 vectors to interpolate");
        }

        /*% Append a 'safety' column on to the end to avoid problems taking *exactly* the last frame */
        /* Zero padding */
//        Complex64Vector zero = new Complex64Vector(new Complex64(0.0, 0.0), input.getNBands());
//        input.appendVector(zero);

        /* 1-Mirror padding */
        input.appendVector(input.getVector(input.getNVectors() - 1));

        /* Load all the magnitudes and phases (optimisation) */
        ArrayList<double[]> magnitudes = new ArrayList<>();
        ArrayList<double[]> phases = new ArrayList<>();
        for (int i = 0; i < input.getNVectors(); i++) {
            Complex64Vector vector = input.getVector(i);
            magnitudes.add(vector.getMagnitudes());
            phases.add(vector.getArguments());
        }

        /* Interpolation */
        double timeDifference;
        double deltaPhase;
        double[] phase1, phase2, magnitude1, magnitude2;
//        double[] magnitudeResult = new double[input.getNBands()];
//        Complex64[] frameResult = new Complex64[input.getNBands()];

        for (int outputColumn = 0; outputColumn < mInterpolatedMatrix.getNVectors(); outputColumn++) {
            int inputColumn = outputColumn / 500;

            /* Grab the two columns */
            phase1 = phases.get(inputColumn);
            phase2 = phases.get(inputColumn + 1);
            magnitude1 = magnitudes.get(inputColumn);
            magnitude2 = magnitudes.get(inputColumn + 1);

            /* Get relative distance between the two vectors */
            timeDifference = outputColumn * mScaleRate - inputColumn;
            /* Rounding allows to have the input vector in the output. If not, the exact value of
            the input vectors does not match because of floating point precision. It is not
            needed in practise, so you can skip it and get lower computational time.
            */
//            int nDecimals = (int) (Math.log10(1 / mScaleRate - 1) + 1);
//            int sf = (int) Math.pow(10, nDecimals);
//            timeDifference = ((double) Math.round(timeDifference * sf) / sf);
//            Log.d(TAG, "interpolateFrames: time/inputcolums/outcolumn/timediff " + (outputColumn * mScaleRate) + "/" + inputColumn + "-" + String.valueOf(inputColumn + 1) + "/" + outputColumn + "/" + timeDifference);

            for (int band = 0; band < magnitude1.length; band++) {
                /* Linear interpolation */
                magnitudeResult[band] = (1 - timeDifference) * magnitude1[band] + timeDifference * magnitude2[band];
                /* Calculate expectedPhaseChange advance, fold the result to [-pi,pi) range */
                deltaPhase = (phase2[band] - phase1[band] - mDeltaPhi[band]) % Math.PI;
//                deltaPhase = deltaPhase - 2 * Math.PI * Math.round(deltaPhase / (2.0 * Math.PI));
                frameResult[band] = Complex64.polar2Rectangular(magnitudeResult[band], mAcumulatedPhase[band]);
//                frameResult[band] = Complex64.fastPolar2Rectangular(magnitudeResult[band], mAcumulatedPhase[band]);
//                frameResult[band] = Complex64.polar2RectangularInC(magnitudeResult[band], mAcumulatedPhase[band]);
                /* Cumulate expectedPhaseChange, ready for next frame */
                mAcumulatedPhase[band] += mDeltaPhi[band] + deltaPhase;
            }
            /* Save the resulting column (vector) */
            mInterpolatedMatrix.setVector(frameResult, outputColumn);
//            if (halfmode) {
//                outputColumn++;
//                try {
//                    mInterpolatedMatrix.setVector(frameResult, outputColumn);
//                } catch (IllegalArgumentException expected) {
//                     Faster to get an exception than implement a check for the outputColumn
//                }
//            }
        }
        return new Complex64Matrix(mInterpolatedMatrix.getPrimitive());
//        return mInterpolatedMatrix;
    }


    /**
     * Intermediate method. The same result than interpolateFrames method but with the recursion
     * swapped.
     *
     * @param input matrix to be processed
     * @return resulting matrix
     */
    private Complex64Matrix interpolateFramesBandThenColumn(Complex64Matrix input) {
        if (input.getNVectors() < 2) {
            throw new IllegalArgumentException("It needs at least 2 vectors to interpolate");
        }

        /*% Append a 'safety' column on to the end to avoid problems taking *exactly* the last frame */
        /* Zero padding */
//        Complex64Vector zero = new Complex64Vector(new Complex64(0.0, 0.0), input.getNBands());
//        input.appendVector(zero);

        /* 1-Mirror padding */
        input.appendVector(input.getVector(input.getNVectors() - 1));

        /* Load all the magnitudes and phases (optimisation) */
        ArrayList<double[]> magnitudes = new ArrayList<>();
        ArrayList<double[]> phases = new ArrayList<>();
        for (int i = 0; i < input.getNBands(); i++) {
            Complex64Vector vector = input.getBand(i);
            magnitudes.add(vector.getMagnitudes());
            phases.add(vector.getArguments());
        }

        /* Interpolation */
        double deltaPhase;
        double magnitudeResult;
        Complex64[] bandResult = new Complex64[mInterpolatedMatrix.getNVectors()];

        /* Pre-compute time differences (optimisation) */
        double[] timeDifference = new double[mInterpolatedMatrix.getNVectors()];
        for (int outputColumn = 0; outputColumn < mInterpolatedMatrix.getNVectors(); outputColumn++) {
            int inputColumn = outputColumn / 500;
            /* Get relative distance between the two vectors */
            timeDifference[outputColumn] = outputColumn * mScaleRate - inputColumn;
        }

        for (int band = 0; band < input.getNBands(); band++) {
            double[] phases_band = phases.get(band);
            double[] magnitudes_band = magnitudes.get(band);

//            InterpolatorCallable interpolator = new InterpolatorCallable(magnitudes_band, phases_band, timeDifference);
//            bandResult = interpolator.interpolate(mInterpolatedMatrix.getNVectors());

            for (int outputColumn = 0; outputColumn < mInterpolatedMatrix.getNVectors(); outputColumn++) {
                int inputColumn = outputColumn / 500;

                /* Grab the two values from this band (they corresponds to different frames) */
                double phase1 = phases_band[inputColumn];
                double phase2 = phases_band[inputColumn + 1];
                double magnitude1 = magnitudes_band[inputColumn];
                double magnitude2 = magnitudes_band[inputColumn + 1];

                /* Linear interpolation */
                magnitudeResult = (1 - timeDifference[outputColumn]) * magnitude1 + timeDifference[outputColumn] * magnitude2;
                /* Calculate expectedPhaseChange advance, fold the result to [-pi,pi) range */
                deltaPhase = (phase2 - phase1 - mDeltaPhi[band]) % Math.PI;
                /* Return to rectangular form */
                bandResult[outputColumn] = Complex64.polar2Rectangular(magnitudeResult, mAcumulatedPhase[band]);
                /* Accumulate expectedPhaseChange, ready for next frame */
                mAcumulatedPhase[band] += mDeltaPhi[band] + deltaPhase;
            }
            try {
                mInterpolatedMatrix.setBand(bandResult, band);
            } catch (IllegalArgumentException expected) {
                /* Faster to get an exception than implement a check for the outputColumn */
            }
        }
        return new Complex64Matrix(mInterpolatedMatrix.getPrimitive());
    }


    /**
     * Interpolate an STFT array according to the PhaseVocoder using multi-threading.
     * <p>
     * It computes the time-samples (t), which specifies a path through the time-base defined by
     * the columns of 'input'. For each value of t, the spectral magnitudes in the columns of
     * 'input' are mInterpolatedMatrix, and the mAcumulatedPhase difference between the successive columns of
     * 'input' is calculated; a new column is created in the output array that preserves this
     * per-step mAcumulatedPhase advance in each bin. 'hop' is needed to calculate the 'null' mAcumulatedPhase advance
     * expected in each bin.
     * <p>
     * Note: t is defined relative to a zero origin, so 0.1 is 90% of the first column of b, plus 10% of the second.
     *
     * @param input an STFT matrix with N/2+1 bands
     * @return the mInterpolatedMatrix matrix
     */
    private Complex64Matrix interpolateThreaded(Complex64Matrix input) {
        if (input.getNVectors() < 2) {
            throw new IllegalArgumentException("It needs at least 2 vectors to interpolate");
        }

        /*% Append a 'safety' column on to the end to avoid problems taking *exactly* the last frame */
        /* Zero padding */
//        Complex64Vector zero = new Complex64Vector(new Complex64(0.0, 0.0), input.getNBands());
//        input.appendVector(zero);

        /* 1-Mirror padding */
        input.appendVector(input.getVector(input.getNVectors() - 1));

        /* Load all the magnitudes and phases (optimisation) */
        ArrayList<double[]> magnitudes = new ArrayList<>();
        ArrayList<double[]> phases = new ArrayList<>();
        for (int i = 0; i < input.getNBands(); i++) {
            Complex64Vector vector = input.getBand(i);
            magnitudes.add(vector.getMagnitudes());
            phases.add(vector.getArguments());
        }

        /* Interpolation */
        double deltaPhase;
        double magnitudeResult;
        Complex64[] bandResult = new Complex64[mInterpolatedMatrix.getNVectors()];

        /* Pre-compute time differences (optimisation) */
        double[] timeDifference = new double[mInterpolatedMatrix.getNVectors()];
        for (int outputColumn = 0; outputColumn < mInterpolatedMatrix.getNVectors(); outputColumn++) {
            int inputColumn = outputColumn / 500;
            /* Get relative distance between the two vectors */
            timeDifference[outputColumn] = outputColumn * mScaleRate - inputColumn;
        }

//        Log.d(TAG, "interpolateThreaded: Avaliable processors: " + Runtime.getRuntime().availableProcessors());
        interpool.addJob(magnitudes, phases, timeDifference, mDeltaPhi, mInterpolatedMatrix.getNVectors(), mAcumulatedPhase);
        mInterpolatedMatrix = interpool.getResult();
        mAcumulatedPhase = interpool.getNextExpectedPhase();

//        return new Complex64Matrix(mInterpolatedMatrix.getPrimitive());
        return mInterpolatedMatrix;
    }

    /**
     * Applies different method for interpolation using a single-thread of multi-thread depending on
     * a parameter
     *
     * @param input         matrix to be interpolated
     * @param multithreaded use multiple threads if true, otherwise use a single thread
     * @return resulting matrix
     */
    private Complex64Matrix interpolate(Complex64Matrix input, boolean multithreaded) {
        if (multithreaded) return interpolateThreaded(input);
        else return interpolateFramesBandThenColumn(input);
    }

    public double getScaleRate() {
        return mScaleRate;
    }

    public int getFftSize() {
        return mFftSize;
    }

    public int getHop() {
        return mHop;
    }

}

//    /**
//     * Interpolate an STFT array according to the 'mAcumulatedPhase vocoder'.
//     * <playerState>
//     * It computes the time-samples (t), which specifies a path through the time-base defined by
//     * the columns of 'input'. For each value of t, the spectral magnitudes in the columns of
//     * 'input' are mInterpolatedMatrix, and the mAcumulatedPhase difference between the successive columns of
//     * 'input' is calculated; a new column is created in the output array that preserves this
//     * per-step mAcumulatedPhase advance in each bin. 'hop' is needed to calculate the 'null' mAcumulatedPhase advance
//     * expected in each bin.
//     * <playerState>
//     * Note: t is defined relative to a zero origin, so 0.1 is 90% of the first column of b, plus 10% of the second.
//     *
//     * @param input : an STFT matrix with N/2+1 bands
//     * @return the mInterpolatedMatrix matrix
//     */
//    @Deprecated
//    private Complex64Matrix pvsample(Complex64Matrix input) {
//        if (input.getNVectors() < 2)
//            throw new IllegalArgumentException("It needs at least 2 vectors to interpolate");
//
//        // We need the last column to interpolate
//        int newNVectors = (int) ((input.getNVectors() - 2 + 1) * (1.0 / mScaleRate) + 1);
//
//        Complex64Matrix out = new Complex64Matrix(new Complex64(0.0, 0.0), newNVectors, input.getNBands());
//
//        //% Expected mAcumulatedPhase advance in each bin
//
//        int N = 2 * (input.getNBands() - 1); //N bands (originally)
//        if (mHop == 0) mHop = N / 2;
//
//        //% Expected mAcumulatedPhase advance in each bin
//        Vector deltaPhi = new Vector(N / 2 + 1); //Vector deltaPhi = new Vector(nRows);
//        deltaPhi.setValueOfIndex(0, 0.0);
//        for (int i = 1; i <= N / 2; i++) {
//            double value = (2 * Math.PI * mHop) / (N / i); //double value = (2 * Math.PI * mHop) * i / N;
//            deltaPhi.setValueOfIndex(i, value);
//        }
//
//        //% Phase accumulator
//        //% Preset to mAcumulatedPhase of first frame for perfect reconstruction
//        //% in case of 1:1 time scaling
//        double[] expectedPhaseChange = input.getVector(0).getArguments();
//
//
//        //% Append a 'safety' column on to the end of b to avoid problems
//        //% taking *exactly* the last frame (i.e. 1*b(:,cols)+0*b(:,cols+1))
//        Complex64Vector zero = new Complex64Vector(new Complex64(0.0, 0.0), input.getNBands());
//        input.appendVector(zero);
//
//        // Interpolates
//        int outputColumn = 0;
//        for (double time = 0; time <= input.getNVectors() - 1 - 2 + 1; time += mScaleRate, outputColumn++) {
//            int timeBase = (int) (Math.floor(time));
//
//            // Grab the two columns of b
//            Complex64Vector bcols1 = input.getVector(timeBase);
//            Complex64Vector bcols2 = input.getVector(timeBase + 1);
//            double[] tmp_phase1 = bcols1.getArguments();
//            double[] tmp_phase2 = bcols2.getArguments();
//            double[] tmp_mag1 = bcols1.getBandMagnitudes();
//            double[] tmp_mag2 = bcols2.getBandMagnitudes();
//
//            // Get relative distance between the two vectors
//            double timeDifference = time - timeBase;
//
//            double[] magnitude = new double[bcols1.getLength()];
//            Complex64[] tmp3 = new Complex64[bcols1.getLength()];
//            double deltaPhase;
//            for (int i = 0; i < bcols1.getLength(); i++) {
//                // Linear interpolation
//                magnitude[i] = (1 - timeDifference) * tmp_mag1[i] + timeDifference * tmp_mag2[i];
//                //% Calculate mAcumulatedPhase advance
//                deltaPhase = tmp_phase2[i] - tmp_phase1[i] - deltaPhi.getValueOfIndex(i);
//                // Reduce to -pi:pi range
//                deltaPhase = deltaPhase - 2 * Math.PI * Math.round(deltaPhase / (2.0 * Math.PI));
////                tmp3[i] = Complex64.polar2Rectangular(magnitude[i], mAcumulatedPhase[i]);
//                tmp3[i] = Complex64.fastPolar2Rectangular(magnitude[i], expectedPhaseChange[i]);
//                //% Cumulate mAcumulatedPhase, ready for next frame
//                expectedPhaseChange[i] += deltaPhi.getValueOfIndex(i) + deltaPhase;
//            }
//            // Save the column (vector)
//            out.setVector(tmp3, outputColumn);
//        }
//
//        return new Complex64Matrix(out.getPrimitive());
//    }
