package ie.ucc.salgadoe.pinbsapp.data;

/**
 * Defines a matrix of Complex64 objects and handles matrix operators
 */
public class Complex64Matrix {
    private static String TAG = "Complex64Matrix";
    private Complex64[][] mMatrix;

    public Complex64Matrix() {
        mMatrix = null;
    }

    /**
     * Create a matrix of complex numbers.
     * <p>
     * Note: It is like a Complex64[nVectors][nBands]
     *
     * @param nVectors number of vectors
     * @param nBands   number of elements per vector
     */
    public Complex64Matrix(int nVectors, int nBands) {
        mMatrix = new Complex64[nVectors][nBands];
    }

    /**
     * Create a matrix of Complex64 with all the values set a given Complex64
     *
     * @param c        a complex number
     * @param nVectors number of vectors
     * @param nBands   number of elements per vectors
     */
    public Complex64Matrix(Complex64 c, int nVectors, int nBands) {
        this(nVectors, nBands);
        for (int i = 0; i < nVectors; i++) {
            for (int j = 0; j < nBands; j++) {
                mMatrix[i][j] = c;
            }
        }
    }

    public Complex64Matrix(Complex64[][] matrix) {
        mMatrix = matrix;
    }

    public Complex64[][] getPrimitive() {
        return mMatrix;
    }

    public void setMatrix(Complex64[][] matrix) {
        if (mMatrix.length == matrix.length && mMatrix[0].length == matrix[0].length) {
            mMatrix = matrix.clone();
        } else throw new IllegalArgumentException("The size does not match.");
    }

    /**
     * Asign an array of Complex64 to one of the vectors of the matrix
     *
     * @param vector   array of Complex64
     * @param position position where to asign the input
     */
    public void setVector(Complex64[] vector, int position) {
        if (position >= mMatrix.length || position < 0) {
            throw new IllegalArgumentException(position + " vectorNumber out of range. There is only (" + mMatrix.length + ") elements");
        } else if (vector.length != mMatrix[0].length) {
            throw new IllegalArgumentException("It is not allowed to change the vector size from (" + mMatrix[0].length + ") to (" + vector.length + ")");
        }
        mMatrix[position] = vector.clone();
    }

    public void setVector(Complex64Vector vector, int position) {
        setVector(vector.getPrimitive(), position);
    }

    public Complex64Vector getVector(int vectorNumber) {
        return new Complex64Vector(mMatrix[vectorNumber]);
    }

    public Complex64Vector getBand(int position) {
        if (position >= mMatrix[0].length || position < 0) {
            throw new IllegalArgumentException(position + " bandNumber out of range. There is only (" + mMatrix[0].length + ") elements");
        } else {
            Complex64[] band = new Complex64[mMatrix.length];
            for (int i = 0; i < mMatrix.length; i++) {
                band[i] = mMatrix[i][position];
            }
            return new Complex64Vector(band);
        }

    }

    public void setBand(Complex64[] band, int position) {
        if (position >= mMatrix[0].length || position < 0) {
            throw new IllegalArgumentException(position + " bandNumber out of range. There is only (" + mMatrix[0].length + ") elements");
        }
        if (band.length != mMatrix.length) {
            throw new IllegalArgumentException("It is not allowed to change the band size from (" + mMatrix[0].length + ") to (" + band.length + ")");
        }
        for (int i = 0; i < mMatrix.length; i++) {
            mMatrix[i][position] = band[i];
        }
    }

    public void setBand(Complex64Vector band, int position) {
        setBand(band.getPrimitive(), position);
    }

    /**
     * Returns the number of vectors and the number of elements per vector (bands)
     *
     * @return {n vectors, n elements per vector}
     */
    private int[] getDimensions() {
        return new int[]{mMatrix[0].length, mMatrix.length};
    }

    public int getNBands() {
        return getDimensions()[0];
    }

    public int getNVectors() {
        return getDimensions()[1];
    }

    /**
     * Serialize a matrix to a vector.
     * <p>
     * Example: input [a1, a2 ; b1, b2; c1 c2] --> output [a1, a2, b1, b2, c1, c2]
     *
     * @return serialized matrix in a vector shape
     */
    public Complex64Vector stackVectors() {
        Complex64[] vector = new Complex64[getNBands() * getNVectors()];
        int k = 0;
        for (int i = 0; i < getNVectors(); i++) {
            for (int j = 0; j < getNBands(); j++) {
                vector[k] = mMatrix[i][j];
                k++;
            }
        }
        return new Complex64Vector(vector);
    }

    /**
     * Returns the conjugate transposed (Hermitian transposed) of the matrix
     *
     * @return matrix of complex numbers
     */
    public Complex64Matrix getConjugated() {
        Complex64Matrix matrix = new Complex64Matrix(getNBands(), getNVectors());
        for (int i = 0; i < getNBands(); i++) {
            matrix.setVector(getBand(i).getConjugated(), i);
        }
        return matrix;
    }

    /**
     * Returns the same matrix but with all the elements conjugated one-by-one
     *
     * @return matrix with conjugated elements
     */
    public Complex64Matrix getConjugatedElements() {
        Complex64Matrix matrix = new Complex64Matrix(getNVectors(), getNBands());
        for (int i = 0; i < getNVectors(); i++) {
            matrix.setVector(getVector(i).getConjugated(), i);
        }
        return matrix;
    }

    /**
     * Inverse short-time Fourier transform using a rectangular window and a fft of the same
     * points as the data.
     * <p>
     * Further information in: public Vector istft(int ftsize, Window w, int hop)
     *
     * @return the istft of the signal
     */
    public Vector istft() {
        return istft(2 * getNBands() - 1);
    }

    /**
     * Inverse short-time Fourier transform using a rectangular window.
     * <p>
     * Further information in: public Vector istft(int ftsize, Window w, int hop)
     *
     * @param ftsize number of points
     * @return the istft of the signal
     */
    public Vector istft(int ftsize) {
        return istft(ftsize, new Window(Window.WINDOW_RECTANGULAR, ftsize));
    }

    public Vector istft(int ftsize, Window window) {
        return window.istft(ftsize, 0, this);
    }

    public void appendVector(Complex64Vector vec) {
        if (vec.getLength() != getNBands()) {
            throw new IllegalArgumentException("Size vectors (number of bands) does not match");
        }

        Complex64Matrix newMatrix = new Complex64Matrix(getNVectors() + 1, getNBands());
        for (int i = 0; i < getNVectors(); i++) {
            newMatrix.setVector(getVector(i), i);
        }
        newMatrix.setVector(vec, newMatrix.getNVectors() - 1);
        mMatrix = newMatrix.mMatrix;
    }

    public String toString() {
        String out = "{\n";
        for (int i = 0; i < mMatrix.length; i++) {
            Complex64Vector vec = new Complex64Vector(mMatrix[i]);
            out += "V" + i + ": " + vec.toString() + "\n";
        }
        out += "}";
        return out;
    }

}
