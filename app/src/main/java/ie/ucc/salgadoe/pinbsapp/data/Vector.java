package ie.ucc.salgadoe.pinbsapp.data;

/**
 * Defines an object to handle a vector of Double's
 */
public class Vector {
    private static final String TAG = "Vector";
    double[] mVector;

    public Vector(int length) {
        mVector = new double[length];
    }

    public Vector(double[] signal) {
        mVector = new double[signal.length];
        System.arraycopy(signal, 0, mVector, 0, signal.length);
    }

    /**
     * Constructor
     *
     * @param signal array of Doubles
     * @deprecated Use {@link #Vector(double[])} instead
     */
    @Deprecated
    public Vector(Double[] signal) {
        for (int i = 0; i < signal.length; i++) {
            mVector[i] = signal[i];
        }
    }

    public Vector(Double d, int length) {
        this(length);
        for (int i = 0; i < length; i++) {
            mVector[i] = d;
        }
    }

    public double[] getDoubles() {
        return mVector;
    }

    public void setSignal(Double[] s) {
        for (int i = 0; i < s.length; i++) {
            mVector[i] = s[i];
        }
    }

    public int getLength() {
        return mVector.length;
    }

    public double getValueOfIndex(int index) {
//        if (index < 0) {
//            throw new IndexOutOfBoundsException(
//                    "[Vector.getValueOfIndex(index)] index must be a positive number");
//        } else return mVector[index];
        return mVector[index];
    }

    public void setValueOfIndex(int index, double value) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(
                    "[Vector.getValueOfIndex(index)] index must be a positive number");
        } else mVector[index] = value;
    }

    /**
     * Returns the mVector as a vector of doubles
     *
     * @return vector of doubles representing the mVector
     */
    public double[] getPrimitive() {
        try {
            double[] out = new double[mVector.length];
            System.arraycopy(mVector, 0, out, 0, out.length);
            return out;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public double getMax() {
        double max = Double.MIN_VALUE;
        for (double aMSignal : mVector) {
            if (aMSignal > max) max = aMSignal;
        }
        return max;
    }

    public double getMin() {
        double min = Double.MAX_VALUE;
        for (double aMSignal : mVector) {
            if (aMSignal < min) min = aMSignal;
        }
        return min;
    }

    /**
     * Scale to [-1, 1]. Note this does not extend to [-1, 1], only ensures that there's no value
     * out of this range.
     */
    public void normalize() {
        double norm = Math.max(Math.abs(getMax()), Math.abs(getMin()));
        scale(norm);
    }

    /**
     * Gets part of the mVector
     *
     * @param start the first index
     * @param stop  the last index (inclusive)
     * @return part of the mVector between two indexes
     */
    public Vector getRange(int start, int stop) {
        if (stop <= start || start < 0 || stop < 0) {
            throw new IllegalArgumentException("[Vector.getRange()] stop (" + stop +
                    ") must be a " + "greater number than connect (" + start +
                    ") and both of them must be positives");
        }
        double[] out = new double[(stop - start + 1)];
        System.arraycopy(mVector, start, out, 0, stop - start + 1);
        return new Vector(out);
    }

    @Override
    public Vector clone() {
        return new Vector(mVector);
    }

    /**
     * Scale the amplitude of the Vector by a factor
     *
     * @param factor scale factor
     */
    public void scale(double factor) {
        double[] scaled = new double[mVector.length];
        for (int i = 0; i < mVector.length; i++) scaled[i] = factor * mVector[i];
        mVector = scaled;
    }

    public void concatenate(Vector vector) {
        double[] newVector = new double[mVector.length + vector.getLength()];
        System.arraycopy(mVector, 0, newVector, 0, mVector.length);
        System.arraycopy(vector.getDoubles(), 0, newVector, mVector.length, vector.getLength());
        mVector = newVector;
    }

    /**
     * Does the dot product of both Vectors
     *
     * @param vector Vector
     * @return the result of the dot product operation between the current and the given mVector
     */
    public Vector dotProduct(Vector vector) {
        if (getLength() != vector.getLength()) {
            throw new IllegalArgumentException("[Vector.dotProduct(in) in (" + vector.getLength() +
                    ") must have the same " + "size as mVector (" + getLength() + ")");
        } else {
            Vector resultingVector = new Vector(getLength());
            for (int c = 0; c < getLength(); c++)
                resultingVector.mVector[c] = mVector[c] * vector.mVector[c];
            return resultingVector;
        }
    }

    /**
     * Computes the N-points Fast Fourier Transform
     *
     * @param nPoints number of points
     * @return the FFT of the Vector
     */
    public Complex64Vector fft(int nPoints) {
        double[] reals = getPrimitive().clone();
        double[] imaginaries = new Vector(0.0, getLength()).getPrimitive();

        FFT transform = new FFT(nPoints, FFT.TIME_TO_FREQ);
        transform.fft(reals, imaginaries);

        Complex64Vector out = new Complex64Vector(new Complex64(0.0, 0.0), reals.length);
        out.setReals(reals);
        out.setImaginaries(imaginaries);
        return out;
    }

    /**
     * Computes the Fast Fourier Transform
     *
     * @return the FFT of the Vector
     */
    public Complex64Vector fft() {
        return fft(mVector.length);
    }

    /**
     * Compute the 256-points Short-time Fourier Transform using a Hann window
     *
     * @return the STFT of the mVector
     */
    public Complex64Matrix stft() {
        Window window = new Window(Window.WINDOW_HANN, 256);
        return stft(256, window, 0);
    }

    /**
     * Compute the N-points Short-time Fourier Transform using a Hann window
     *
     * @param nPoints number of points
     * @return the STFT of the mVector
     */
    public Complex64Matrix stft(int nPoints) {
        Window window = new Window(Window.WINDOW_HANN, nPoints);
        return stft(nPoints, window, 0);
    }

    /**
     * Compute the N-points Short-time Fourier Transform
     *
     * @param fftPoints number of points
     * @param window    window definition
     * @param hop       hop size
     * @return the STFT of the Vector
     */
    public Complex64Matrix stft(int fftPoints, Window window, int hop) {
        /* default hop */
        if (hop == 0) hop = (int) Math.floor(window.getLength() / 2);

        int nVectors = (int) Math.floor((mVector.length - fftPoints) / hop) + 1;

        Complex64Matrix matrix = new Complex64Matrix(nVectors, 1 + fftPoints / 2);
        for (int pos = 0, vector = 0; pos <= (mVector.length - fftPoints); pos += hop, vector++) {
            Vector mSignalChunk = getRange(pos, pos + fftPoints - 1);
            Vector windowed = mSignalChunk.dotProduct(window);
            Complex64Vector t = windowed.fft();
            matrix.setVector(t.getRange(0, fftPoints / 2), vector);
        }
        return matrix;
    }

    /**
     * Returns the mix of two vectors, shifting the vector 'v' by 'shift' samples and doing the sum
     * of their values
     *
     * @param vector a that will be shift
     * @param shift  number of positions to shift
     * @return the result of summing the two vectors
     */
    public Vector mix(Vector vector, int shift) {
        if (shift > getLength()) {
            throw new IllegalArgumentException("hop cannot be longer thant connect vector1");
        }

        double[] shifted = vector.getPrimitive();
        int len = Math.max(mVector.length, shift + shifted.length);
        double[] resultingVector = new double[len];

        for (int i = 0; i < resultingVector.length; i++) {
            resultingVector[i] = 0;
            try {
                resultingVector[i] += mVector[i];
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            try {
                resultingVector[i] += shifted[i - shift];
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }

        }
        return new Vector(resultingVector);
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[Double.SIZE / Byte.SIZE * mVector.length];
        for (int i = 0; i < mVector.length; i++) {
            byte[] sample = DataConverter.DoubleToByteArray(mVector[i]);
            System.arraycopy(sample, 0, bytes, i * 8, sample.length);
        }
        return bytes;
    }

    public byte[] get16MSB() {
        byte[] bytes = new byte[Short.SIZE / Byte.SIZE * mVector.length];
        for (int i = 0; i < mVector.length; i++) {
            byte[] sample = DataConverter.DoubleToByteArray(mVector[i]);
            System.arraycopy(sample, 0, bytes, i * (Short.SIZE / Byte.SIZE), Short.SIZE / Byte.SIZE);
        }
        return bytes;
    }

    public byte[] get16LSB() {
        byte[] bytes = new byte[Short.SIZE / Byte.SIZE * mVector.length];
        for (int i = 0; i < mVector.length; i++) {
            byte[] sample = DataConverter.DoubleToByteArray(mVector[i]);
            System.arraycopy(sample, (Double.SIZE - Short.SIZE) / Byte.SIZE,
                    bytes, i * (Short.SIZE / Byte.SIZE),
                    Short.SIZE / Byte.SIZE);
        }
        return bytes;
    }

    public String toString() {
        String out = "[ ";
        for (double aMSignal : mVector) {
            out += aMSignal + " ";
        }
        out += "]";
        return out;
    }

    /* TODO: check method Vector.insert(Vector, int) */
    public void insert(Vector s, int i) {
        if (this.getLength() < i + s.getLength()) {
            throw new IllegalArgumentException("Impossible to insert, too long vector o too far position");
        }
        System.arraycopy(s.getPrimitive(), 0, mVector, i, s.getLength());
    }
}
