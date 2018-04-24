package ie.ucc.salgadoe.pinbsapp.data;

import android.util.Log;

public abstract class Complex<N extends Number & Comparable> {
//public abstract class Complex<N extends Number & Comparable<? super N>> {
    /**
     * Defines an object that handle complex numbers
     */
    private static double PRECISION;
    private static int fastPolar2Cartesian_phaseTemp = 0;   // (Optimisation)
    private static double[] LUT_COS;
    private static double[] LUT_SIN;
    private static String TAG = "Complex";

    static {
        try {
            System.loadLibrary("native-lib");
        } catch (final UnsatisfiedLinkError e) {
            Log.e(TAG, "loadLibrary" + Log.getStackTraceString(e));
        }
    }

    protected N mReal, mImaginary;

//    public Complex() {
//        mReal = 0.0;
//        mImaginary = 0.0;
//    }

    public Complex(N real, N imaginary) {
        mReal = real;
        mImaginary = imaginary;
    }

    public N getReal() {
        return mReal;
    }

    public void setReal(N real) {
        mReal = real;
    }

    public N getImaginary() {
        return mImaginary;
    }

    public void setImaginary(N imaginary) {
        mImaginary = imaginary;
    }

    /**
     * Returns the argument
     *
     * @return angle that forms x-axis with the vector
     */
    public double getArgument() {
//        Double arg = Math.atan2(mImaginary, mReal);
//        if (arg.isNaN()) throw new ArithmeticException("One of the numbers is NaN");
//        return arg;
        return Math.atan2(mImaginary.doubleValue(), mReal.doubleValue());
    }

    /**
     * Returns the complex conjugate
     *
     * @return complex conjugate
     */
    public abstract Complex<N> getConjugated();

    public String toString() {
        String out = "";
        out += mReal.toString();
        if (mImaginary.compareTo(0.0) >= 0) out += "+";
//        else out+= "-";
        out += mImaginary.toString() + "j";
        return out;
    }

    /**
     * Returns the magnitude (modulus)
     *
     * @return magnitude of the complex
     */
    public double getMagnitude() {
//        Double arg = Math.hypot(mReal, mImaginary);
//        if (arg.isNaN() || arg.isInfinite())
//            throw new ArithmeticException("One of the numbers is infinite or NaN");
//        return arg;
//        return Math.hypot(mReal.doubleValue(), mImaginary.doubleValue());
        return Math.hypot(mReal.doubleValue(), mImaginary.doubleValue());
    }

//        return new Complex<N>(mReal, mImaginary);
//    }

//
//    /**
//     * Change the complex representation in polar to cartesian.
//     *
//     * @param magnitude magnitude of the vector
//     * @param phase     the phase of the vector in radians
//     * @return complex number in cartesian mode
//     */
//    public static Complex polar2Rectangular(double magnitude, double phase) {
////        double real = magnitude * Math.cos(phase);
////        double imag = magnitude * Math.sin(phase);
//        return new Complex(magnitude * Math.cos(phase), magnitude * Math.sin(phase));
//    }
//
//
//    /**
//     * Change the complex representation in polar to cartesian, using a LUT.
//     * The performance optimization is really small compared to 'polar2Rectangular'.
//     *
//     * @param magnitude magnitude of the vector
//     * @param phase     the phase of the vector in radians
//     * @return complex number in cartesian mode
//     * @see #polar2Rectangular(double, double)
//     */
//    /* TODO: is not working properly, furthermore there is no processing saves */
//    public static Complex fastPolar2Rectangular(double magnitude, double phase) {
//        /* Reduce range to (-pi, pi) */
//        /* Compute position in LUT */
//        fastPolar2Cartesian_phaseTemp = (int) (((phase % Math.PI) + Math.PI) * PRECISION / (2 * Math.PI));
//        return new Complex(magnitude * LUT_COS[fastPolar2Cartesian_phaseTemp], magnitude * LUT_SIN[fastPolar2Cartesian_phaseTemp]);
//    }
//
//    /**
//     * Creates a Look-Up-Table for sine and cosine operations
//     *
//     * @param precision number elements composing the LUT
//     */
//    public static void generateTrigonometricLUT(int precision) {
//        PRECISION = (double) precision;
//        LUT_COS = new double[precision];
//        LUT_SIN = new double[precision];
//        double x;
//        for (int i = 0; i < precision; i++) {
//            x = -Math.PI + i / PRECISION * 2.0 * Math.PI;
//            LUT_COS[i] = Math.cos(x);
//            LUT_SIN[i] = Math.cos(x);
//        }
////        for (double i = -1; i < 1; i += 0.01) {
////            double phase = i * Math.PI;
////            double mod = phase % Math.PI;
////            double pos = (((phase % Math.PI) + Math.PI) * PRECISION / (2 * Math.PI));
////            Log.d(TAG, "generateTrigonometricLUT: phase " + phase + " mod " + mod + " pos " + pos);
////        }
//    }
//
//    public double getReal() {
//        return mReal;
//    }
//
//    public void setReal(double real) {
//        mReal = real;
//    }
//
//    public double getImaginary() {
//        return mImaginary;
//    }
//
//    public void setImaginary(double imaginary) {
//        mImaginary = imaginary;
//    }
//
//    /**
//     * Returns the argument
//     *
//     * @return angle that forms x-axis with the vector
//     */
//    public double getArgument() {
////        Double arg = Math.atan2(mImaginary, mReal);
////        if (arg.isNaN()) throw new ArithmeticException("One of the numbers is NaN");
////        return arg;
//        return Math.atan2(mImaginary, mReal);
//    }
//


//
//    /**
//     * Faster implementation of getArguments
//     *
//     * @return argument of the complex
//     * @see #getArgument()
//     */
//    public double getArgumentFast(int order) {
//        double coef = mImaginary / mReal;
//        Double tmp = coef;
//        for (int i = 1; i < order; i++) {
//            tmp += Math.pow(-1, i % 2) / (2 * i + 1) * Math.pow(coef, 2 * i + 1);
//        }
//        if (tmp.isNaN()) throw new ArithmeticException("One of the numbers is NaN");
//        return tmp;
//    }
//
//    /**
//     * Returns the complex conjugate
//     *
//     * @return complex conjugate
//     */
//    public ie.ucc.jonatanpoveda.data.Complex64 getConjugated() {
//        return new ie.ucc.jonatanpoveda.data.Complex64(mReal, -mImaginary);
//    }
//
//    public String toString() {
//        String out = "";
//        out += Double.valueOf(mReal).toString();
//        if (mImaginary >= 0) out += "+";
////        else out+= "-";
//        out += Double.valueOf(mImaginary).toString() + "j";
//        return out;
//    }
}

