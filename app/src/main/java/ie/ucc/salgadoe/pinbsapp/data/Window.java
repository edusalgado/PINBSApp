package ie.ucc.salgadoe.pinbsapp.data;

/**
 * Defines a window function. Implemented windows: rectangular, Hann.
 */
public class Window extends Vector {
    /* Window types */
    public static final int WINDOW_RECTANGULAR = 0;
    public static final int WINDOW_HANN = 1;

    private static String TAG = "Window";

    public Window(int length) {
        super(length);
        mVector = createWindow(WINDOW_RECTANGULAR, length, length).getDoubles();
    }

    public Window(double[] signal) {
        super(signal);
    }

    /**
     * @deprecated Use {@link #Window(double[])} instead
     */
    @Deprecated
    public Window(Double[] signal) {
        super(signal);
    }

    public Window(Double d, int length) {
        super(d, length);
    }

    public Window(int type, int length) {
        super(length);
        mVector = createWindow(type, length, length).getDoubles();
    }

    public Window(int type, int length, int fftPoints) {
        super(length);
        mVector = createWindow(type, length, fftPoints).getDoubles();
    }

    private Vector createWindow(int type, int length, int fftPoints) {
        if (fftPoints - length < 0) {
            throw new IllegalArgumentException("fftPoints must be greater or equal than windowLength");
        }

        Vector padding = new Vector(0.0, (fftPoints - length) / 2);

        /* Force window to be odd-len */
        if (length % 2 == 0) length++;

        double[] lobe = new double[length];

        switch (type) {
            /* Rectangular window */
            case 0:
                for (int i = 0; i < lobe.length; i++) lobe[i] = 1;
                break;

            /* Hann window */
            case 1:
                int halfLenght = (length - 1) / 2;
                double winValue;
                for (int i = 0; i <= halfLenght + 1; i++) {
                    winValue = 0.5 * (1 - Math.cos(Math.PI * i / halfLenght));
                    lobe[i] = winValue;
                    lobe[length - 1 - i] = winValue;
                }
                break;

            default:
//                Log.d(TAG, "createWindow: Applying rectangular window! Are you sure ?");
                return createWindow(WINDOW_RECTANGULAR, length, length);
        }

        /* Add zero-padding */
        Vector window = padding.clone();
        window.concatenate(new Vector(lobe));
        window.concatenate(padding);

        /* Delete the last sample to have an even-length window (FFT optimization) */
        double[] w = new double[window.getLength() - 1];
        System.arraycopy(window.getPrimitive(), 0, w, 0, window.getLength() - 1);
        window = new Vector(w);

        return window;
    }

    @Override
    public Window clone() {
        super.clone();
        return new Window(mVector);
    }

    /**
     * Inverse short-time Fourier transform
     * <p>
     * Performs overlap-add resynthesis from the short-time Fourier transform data.
     * Each column is taken as the result of an F-point fft; each successive frame was offset
     * by 'hop' points (default window length / 2, or ftsize / 2 if W==0).
     * <p>
     * Data is hann-windowed at W pts, or W = 0 gives a rectangular window (default);
     * W as a vector uses that as window.
     * <p>
     * This version scales the output so the loop gain is 1.0 for either hann-win an-syn with 25%
     * overlap, or hann-win on analysis and rect-win (W=0) on synthesis with 50% overlap.
     * <p>
     * dpwe 1994may24.  Uses built-in 'ifft' etc.
     * $Header: /home/empire6/dpwe/public_html/resources/matlab/pvoc/RCS/istft.m,v 1.5 2010/08/12 20:39:42 dpwe Exp $
     *
     * @param fftSize       number of points
     * @param hop           hop
     * @param complexMatrix
     * @return the real part of the signal's istft
     */
    public Vector istft(int fftSize, int hop, Complex64Matrix complexMatrix) {
        /* Checks the signal and fftsize to match */
        if (complexMatrix.getNBands() != fftSize / 2 + 1) {
            throw new IllegalArgumentException("number of rows should be fftsize/2+1");
        }
        int numberOfColumns = complexMatrix.getNVectors();

        if (hop == 0) hop = (int) Math.floor(getLength() / 2.0);

        int xlen = fftSize + (numberOfColumns - 1) * hop;
//        int xlen = ftsize + (cols - 2) * hop;
        double[] outputVector = new Vector(0.0, xlen).getPrimitive();

        int start = fftSize / 2 - 1;
        for (int b = 0, n = 0; b <= hop * (numberOfColumns - 1); b += hop, n++) {
//        for (int b = 0; b <= hop * (cols - 2); b += hop) {
            Complex64Vector vector = complexMatrix.getVector(n);

            /* Rebuild complete frame with its hermitian */
            Complex64Vector tmp = vector.getRange(start, 1).getConjugated();
            Complex64[] ft2 = new Complex64[vector.getLength() + tmp.getLength()];
            System.arraycopy(vector.getPrimitive(), 0, ft2, 0, vector.getLength());
            System.arraycopy(tmp.getPrimitive(), 0, ft2, vector.getLength(), tmp.getLength());

            /* In-place ifft */
            FFT ifft = new FFT(fftSize, FFT.FREQ_TO_TIME);
            Complex64Vector ft3 = new Complex64Vector(ft2);
            double[] reals = ft3.getReals();
            double[] imag = ft3.getImaginaries();
            ifft.fft(reals, imag);

            /* Windowing */
//            double[] px = reals;
            Vector pxSignal = new Vector(reals);
            double[] tmp2 = pxSignal.dotProduct(this).getDoubles();                 // Primitive();

            /* TODO: try to change the next 'for' to Vector.mix() */
//            Vector tmp3 = pxSignal.dotProduct(w);
//            Vector tmp4 = tmp3.mix(pxSignal, b);
//            Log.d(TAG, "istft: x " + new Vector(x).toString());
//            Log.d(TAG, "istft: tmp4 " + tmp3);
            for (int i = b, j = 0; i < b + fftSize; i++, j++) {
                outputVector[i] = outputVector[i] + tmp2[j];
            }

        }
        return new Vector(outputVector);
    }
}