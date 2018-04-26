package ie.ucc.salgadoe.pinbsapp.data;

/**
 * Defines a vector of Complex64 objects and handles complex number operators
 */
public class Complex64Vector {
    private static final String TAG = "Complex64Vector";
    private Complex64[] mCoef;
//    private List<Complex64> mCoef;

    public Complex64Vector() {
        mCoef = null;
    }

    public Complex64Vector(int length) {
        mCoef = new Complex64[length];
//        mCoef = Arrays.asList(new Complex64[length]);
    }

    public Complex64Vector(Complex64 complex, int length) {
        this(length);
        for (int i = 0; i < length; i++) {
            mCoef[i] = complex;
        }
    }

    public Complex64Vector(Complex64[] coef) {
//        List<Complex64> mCoef = Arrays.asList(coef);
        mCoef = coef;
    }

    public Complex64[] getPrimitive() {
        return mCoef;
    }

    /**
     * Returns a copy of the real numbers inside the vector
     *
     * @return the real part of all the complex contained in the vector of complex numbers
     */
    public double[] getReals() {
        double[] reals = new double[mCoef.length];
        for (int i = 0; i < mCoef.length; i++) {
            reals[i] = mCoef[i].getReal();
        }
        return reals;
    }

    public void setReals(double[] in) {
        if (mCoef.length != in.length) throw new IllegalArgumentException("Size!");
        for (int i = 0; i < mCoef.length; i++) {
            /* The next line does not work for some dark reason */
//            mCoef[i].setReal(in[i]);
            mCoef[i] = new Complex64(in[i], mCoef[i].getImaginary());
        }
    }

    public void setReals(Vector vector) {
        setReals(vector.getDoubles());
    }

    public void setReals(Double[] reals) {
        double[] newReals = new double[reals.length];
        System.arraycopy(reals, 0, newReals, 0, reals.length);
        setReals(newReals);
    }

    /**
     * Returns a copy of the imaginaries numbers inside the vector
     *
     * @return the imaginary part of all the complex contained in the vector of complex numbers
     */
    public double[] getImaginaries() {
        double[] out = new double[mCoef.length];
        for (int i = 0; i < mCoef.length; i++) {
            out[i] = mCoef[i].getImaginary();
        }
        return out;
    }

    public void setImaginaries(double[] in) {
        if (mCoef.length != in.length) throw new IllegalArgumentException("Size!");
        for (int i = 0; i < mCoef.length; i++) {
            /* The next line does not work for some dark reason */
//            mCoef[i].setImaginary(in[i]);
            mCoef[i] = new Complex64(mCoef[i].getReal(), in[i]);
        }
    }

    public void setImaginaries(Vector in) {
        setImaginaries(in.getDoubles());
    }

    public void setImaginaries(Double[] in) {
        double[] primitive = new double[in.length];
        System.arraycopy(in, 0, primitive, 0, in.length);
        setImaginaries(primitive);
    }

    public int getLength() {
        return mCoef.length;
    }

    /**
     * Returns a copy of the values in the position from 'connect' to 'stop' (both included)
     *
     * @param start starting position
     * @param stop  ending position (inclusive)
     * @return a sub-vector from the original one
     */
    public Complex64Vector getRange(int start, int stop) {
        if (start < 0 || stop < 0) {
            throw new IllegalArgumentException("[Vector.getRange()] the arguments must be positives");
        }
        if (start > mCoef.length - 1 || stop > mCoef.length - 1) {
            throw new IllegalArgumentException("[Vector.getRange()] arguments are out of range");
        }
        if (stop == start) {
            Complex64[] out = new Complex64[1];
            out[0] = mCoef[start];
            return new Complex64Vector(out);
        }

        Complex64[] out = new Complex64[(Math.abs(stop - start) + 1)];
        /* Reverse the output */
        if (stop < start) {
            for (int i = 0, j = start; i < out.length; i++, j--) {
                out[i] = mCoef[j];
            }
        } else System.arraycopy(mCoef, start, out, 0, stop - start + 1);
        return new Complex64Vector(out);
    }

    /**
     * Returns the conjugated vector
     *
     * @return vector of the conjugates of each element
     */
    public Complex64Vector getConjugated() {
        Complex64[] conjugates = new Complex64[mCoef.length];
        for (int i = 0; i < mCoef.length; i++) {
            conjugates[i] = mCoef[i].getConjugated();
        }
        return new Complex64Vector(conjugates);
    }

    /**
     * Returns the argument (complex's angle) of all the elements
     *
     * @return vector of arguments
     */
    public double[] getArguments() {
        double[] arguments = new double[mCoef.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = mCoef[i].getArgument();
        }
        return arguments;
    }

    /**
     * Returns the magnitude (complex's module) of all the elements
     *
     * @return vector of magnitudes
     */
    public double[] getMagnitudes() {
        double[] magnitudes = new double[mCoef.length];
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = mCoef[i].getMagnitude();
        }
        return magnitudes;
    }

    public String toString() {
        String out = "[ ";
        for (Complex64 coef : mCoef) {
            out += coef.toString() + " ";
        }
        out += "]";
        return out;
    }

}

