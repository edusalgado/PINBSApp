package ie.ucc.salgadoe.pinbsapp.data;

import android.util.Log;

/**
 * Defines an object that handle complex numbers
 */
public class Complex64 extends Complex<Double> {
    private static double PRECISION;
    private static int fastPolar2Cartesian_phaseTemp = 0;   // (Optimisation)
    private static double[] LUT_COS;
    private static double[] LUT_SIN;
    private static String TAG = "Complex64";
    /* Used for performance, take care when using it */
    private static Complex64 fastPolar2Cartesian_out = new Complex64();
    private static Complex64 badIdeaTemp = new Complex64(0.0, 0.0);


    static {
        try {
            System.loadLibrary("native-lib");
        } catch (final UnsatisfiedLinkError e) {
            Log.e(TAG, "loadLibrary" + Log.getStackTraceString(e));
        }
    }

//    private double mReal, mImaginary;

    public Complex64() {
        super(0.0, 0.0);
    }

    public Complex64(double real, double imaginary) {
        super(real, imaginary);
    }

    /**
     * Polar 2 Cartesian writen in C++
     **/

    //C++ ¿? Edu.
    public static native double[] CPolar2Rectangular(double magnitude, double argument);

    /**
     * Change the complex representation in polar to cartesian.
     *
     * @param magnitude magnitude of the vector
     * @param phase     the phase of the vector in radians
     * @return complex number in cartesian mode
     */
    public static Complex64 polar2Rectangular(double magnitude, double phase) {
//        double real = magnitude * Math.cos(phase);
//        double imag = magnitude * Math.sin(phase);
        return new Complex64(magnitude * Math.cos(phase), magnitude * Math.sin(phase));
    }

    /**
     * Converts a complex from polar to rectangular form
     *
     * @deprecated it took MORE time than polar2Rectangular, do not use
     **/
    @Deprecated
    public static Complex64 polar2RectangularInC(double magnitude, double phase) {
        double[] c = CPolar2Rectangular(magnitude, phase);
        return new Complex64(c[0], c[1]);
    }

    /**
     * Change the complex representation in polar to cartesian, using a LUT.
     * The performance optimization is really small compared to 'polar2Rectangular'.
     *
     * @param magnitude magnitude of the vector
     * @param phase     the phase of the vector in radians
     * @return complex number in cartesian mode
     * @see #polar2Rectangular(double, double)
     */
    /* TODO: is not working properly, furthermore there is no processing saves */
    public static Complex64 fastPolar2Rectangular(double magnitude, double phase) {
        /* Reduce range to (-pi, pi) */
        /* Compute position in LUT */
        fastPolar2Cartesian_phaseTemp = (int) (((phase % Math.PI) + Math.PI) * PRECISION / (2 * Math.PI));
        return new Complex64(magnitude * LUT_COS[fastPolar2Cartesian_phaseTemp], magnitude * LUT_SIN[fastPolar2Cartesian_phaseTemp]);
    }

    /**
     * Creates a Look-Up-Table for sine and cosine operations
     *
     * @param precision number elements composing the LUT
     */
    public static void generateTrigonometricLUT(int precision) {
        PRECISION = (double) precision;
        LUT_COS = new double[precision];
        LUT_SIN = new double[precision];
        double x;
        for (int i = 0; i < precision; i++) {
            x = -Math.PI + i / PRECISION * 2.0 * Math.PI;
            LUT_COS[i] = Math.cos(x);
            LUT_SIN[i] = Math.cos(x);
        }
    }

    /**
     * Faster implementation of getArguments
     *
     * @return argument of the complex
     * @see #getArgument()
     */
    public double getArgumentFast(int order) {
        double coef = mImaginary / mReal;
        Double tmp = coef;
        for (int i = 1; i < order; i++) {
            tmp += Math.pow(-1, i % 2) / (2 * i + 1) * Math.pow(coef, 2 * i + 1);
        }
        if (tmp.isNaN()) throw new ArithmeticException("One of the numbers is NaN");
        return tmp;
    }

    @Override
    public Complex64 getConjugated() {
        return new Complex64(mReal, -mImaginary);
    }


}
