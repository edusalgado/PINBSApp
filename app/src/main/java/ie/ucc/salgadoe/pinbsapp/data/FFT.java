/**
 * TODO: change the algorithm for one that works only with REAL input (faster), or returns Polar instead of Cartesian: Bruun's_FFT_algorithm,
 * Wiki: In many applications, the input data for the DFT are purely real, in which case the outputs
 * satisfy the symmetry and efficient FFT algorithms have been designed for this situation (see e.g. Sorensen, 1987).
 * One approach consists of taking an ordinary algorithm (e.g. Cooley–Tukey) and removing the
 * redundant parts of the computation, saving roughly a factor of two in time and memory.
 * Alternatively, it is possible to express an even-length real-input DFT as a complex DFT of half
 * the length (whose real and imaginary parts are the even/odd elements of the original real data),
 * followed by O(N) post-processing operation
 * <p>
 * Found FHT (Fast Harley Transform), the fastest, but only apreciable went N is grows. At N=32 is practically the same as others (like Radix2). At N=1024 can be the 1/3 part, at N=16k the 1/2..
 */

package ie.ucc.salgadoe.pinbsapp.data;

/**
 * Implements Fast Fourier Transform using an in-place radix-2 DIT DFT of a complex input
 * (Cooley–Tukey algorithm). FFT and iFFT depending on the initialization of the instance.
 */
public class FFT {
    public static final int TIME_TO_FREQ = 1;                   // DFT
    public static final int FREQ_TO_TIME = -1;                  // iDFT
    private static final String TAG = "FFT";
    private int mNumPoints, mNumPartitions, mMode;              // N, M, mode

    /* Lookup tables.  Only need to recompute when size of FFT changes. */
    private double[] cos;
    private double[] sin;

    FFT(int numPoints, int mode) {
        mNumPoints = numPoints;
        mNumPartitions = (int) (Math.log(numPoints) / Math.log(2));
        mMode = mode;

        /* Make sure mNumPoints is a power of 2 */
        if (numPoints != (1 << mNumPartitions))
            throw new RuntimeException("FFT length must be power of 2");

        /* Check input mMode */
        if (mode != TIME_TO_FREQ && mode != FREQ_TO_TIME) {
            throw new IllegalArgumentException("mMode does not match");
        }

        /* Precompute tables */
        cos = new double[numPoints];
        sin = new double[numPoints];
        double[] cosTemp = new double[numPoints + 1];
        double[] sinTemp = new double[numPoints + 1];

        /* This code does mNumPoints loops, 2*mNumPoints times the 'cos' or 'sin' function */
//        for (int i = 0; i < mNumPoints; i++) {
//            cos[i] = Math.cos(-2 * Math.PI * i / mNumPoints);
//            sin[i] = Math.sin(-2 * Math.PI * i / mNumPoints);
//        }

        /* This code does mNumPoints/4+1 loops, mNumPoints/4+1 times the 'cos' */
        for (int i = 0; i <= numPoints / 4; i++) {
            double value = Math.cos(-2 * Math.PI * i / numPoints);

            cosTemp[i] = value;
            cosTemp[numPoints / 2 + i] = -value;
            cosTemp[numPoints / 2 - i] = -value;
            cosTemp[numPoints - i] = value;

            value *= mode;
            sinTemp[numPoints / 4 + i] = -value;
            sinTemp[numPoints * 3 / 4 + i] = value;
            sinTemp[numPoints / 4 - i] = -value;
            sinTemp[numPoints * 3 / 4 - i] = value;
        }

        /* Discard the last value */
        System.arraycopy(cosTemp, 0, cos, 0, cos.length);
        System.arraycopy(sinTemp, 0, sin, 0, sin.length);

    }

    /**
     * Apply the half of the scale from ifft
     *
     * @param x vector of reals
     * @param y vector of imaginaries
     */
    private void scale(double[] x, double[] y) {
        for (int l = 0; l < x.length; l++) {
            x[l] /= Math.sqrt(mNumPoints);
            y[l] /= Math.sqrt(mNumPoints);
        }
    }


    /**
     * Returns the mMode (fft or ifft) of the transformation
     *
     * @return integer meaning:  1 = fft, -1 = ifft
     */
    public int getmMode() {
        return mMode;
    }

    /***************************************************************
     * fft.c
     * Douglas L. Jones
     * University of Illinois at Urbana-Champaign
     * January 19, 1992
     * http://cnx.rice.edu/content/m12016/latest/
     * <p>
     * fft: in-place radix-2 DIT DFT of a complex input
     * <p>
     * input:
     * mNumPoints: length of FFT: must be a power of two
     * mNumPartitions: mNumPoints = 2**mNumPartitions
     * input/output
     * x: double array of length mNumPoints with real part of data
     * y: double array of length mNumPoints with imag part of data
     * <p>
     * Permission to copy and use this program is granted
     * as long as this header is included.
     ****************************************************************/
    void fft(double[] x, double[] y) {
        int i, j, k, n1, n2, a;
        double c, s, e, t1, t2;

        /* Bit-reverse */
        j = 0;
        n2 = mNumPoints / 2;
        for (i = 1; i < mNumPoints - 1; i++) {
            n1 = n2;
            while (j >= n1) {
                j = j - n1;
                n1 = n1 / 2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        /* FFT */
        n1 = 0;
        n2 = 1;

        for (i = 0; i < mNumPartitions; i++) {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for (j = 0; j < n1; j++) {
                c = cos[a];
                s = sin[a];
                a += 1 << (mNumPartitions - i - 1);

                for (k = j; k < mNumPoints; k = k + n2) {
                    t1 = c * x[k + n1] - s * y[k + n1];
                    t2 = s * x[k + n1] + c * y[k + n1];
                    x[k + n1] = x[k] - t1;
                    y[k + n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }

        scale(x, y);
    }

}
