package ie.ucc.salgadoe.pinbsapp.data;

import  ie.ucc.salgadoe.pinbsapp.audio.InterpolatorCallable;


/**
 * Container for InterpolatorCallable's result. See {@link InterpolatorCallable#call()}
 *
 * @see InterpolatorCallable
 */
public class InterpolatorResult {
    private Complex64[] mBand;
    private double mPhase;

    public InterpolatorResult(Complex64[] band, double phase) {
        mBand = band;
        mPhase = phase;
    }

    public Complex64[] getBand() {
        return mBand;
    }

    public void setBand(Complex64[] c) {
        mBand = c;
    }

    public double getPhase() {
        return mPhase;
    }

    public void setPhase(double phase) {
        mPhase = phase;
    }
}
