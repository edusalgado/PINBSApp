package ie.ucc.salgadoe.pinbsapp.audio;

import android.os.Process;

import java.util.concurrent.Callable;

import ie.ucc.salgadoe.pinbsapp.data.Complex64;
import ie.ucc.salgadoe.pinbsapp.data.InterpolatorResult;
import ie.ucc.salgadoe.pinbsapp.data.Job;


/* TODO: change this really bad description, for God's sake! */

/**
 * Interpolates the magnitudes and modifies the phase of a band following Phase Vocoder's method
 */
public class InterpolatorCallable implements Callable<InterpolatorResult> {
    private static final String TAG = "InterpolatorCallable";
    private Job mJob;
    private Complex64[] bandResult;

    InterpolatorCallable(Job job) {
        mJob = job;
        bandResult = new Complex64[mJob.getNumberOfInterpolatedVectors()];
    }

    private Complex64[] interpolate() {
//        long j = 0;
//        for (int i = 0; i < 1000000000; i++) {
//            j++;
//        }
        double magnitudeResult, deltaPhase;
        int numberOfOutputColumns = mJob.getNumberOfInterpolatedVectors();
        double[] timeDiff = mJob.getTimeDifferences();
        double[] bandPhases = mJob.getBandPhases();
        double[] bandMagnitudes = mJob.getBandMagnitudes();
        for (int outputColumn = 0; outputColumn < numberOfOutputColumns; outputColumn++) {
            int inputColumn = outputColumn / mJob.getRatio();

            /* Grab the two values from this band (they corresponds to different frames) */
//            double phase1 = bandPhases[inputColumn];
//            double phase2 = bandPhases[inputColumn + 1];
//            double magnitude1 = bandMagnitudes[inputColumn];
//            double magnitude2 = bandMagnitudes[inputColumn + 1];

            /* Linear interpolation */
            magnitudeResult = (1 - timeDiff[outputColumn]) * bandMagnitudes[inputColumn] + timeDiff[outputColumn] * bandMagnitudes[inputColumn + 1];
            /* Calculate expectedPhaseChange advance, fold the result to [-pi,pi) range */
            deltaPhase = (bandPhases[inputColumn + 1] - bandPhases[inputColumn] - mJob.getDeltaPhi()) % Math.PI;
            /* Return to rectangular form */
            bandResult[outputColumn] = Complex64.polar2Rectangular(magnitudeResult, mJob.getAcumulatedPhase());
            /* Accumulate expectedPhaseChange, ready for next frame */
            mJob.setAcumulatedPhase(mJob.getAcumulatedPhase() + mJob.getDeltaPhi() + deltaPhase);

//            /* Grab the two values from this band (they corresponds to different frames) */
//            double phase1 = bandPhases[inputColumn];
//            double phase2 = bandPhases[inputColumn + 1];
//            double magnitude1 = bandMagnitudes[inputColumn];
//            double magnitude2 = bandMagnitudes[inputColumn + 1];
//
//            /* Linear interpolation */
//            magnitudeResult = (1 - timeDiff[outputColumn]) * magnitude1 + timeDiff[outputColumn] * magnitude2;
//            /* Calculate expectedPhaseChange advance, fold the result to [-pi,pi) range */
//            deltaPhase = (phase2 - phase1 - mJob.getDeltaPhi()) % Math.PI;
//            /* Return to rectangular form */
//            bandResult[outputColumn] = Complex64.polar2Rectangular(magnitudeResult, mJob.getAcumulatedPhase());
//            /* Accumulate expectedPhaseChange, ready for next frame */
//            mJob.setAcumulatedPhase(mJob.getAcumulatedPhase() + mJob.getDeltaPhi() + deltaPhase);
        }

        return bandResult;
    }

    @Override
    public InterpolatorResult call() throws Exception {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + 3);
        return new InterpolatorResult(interpolate(), mJob.getAcumulatedPhase());
    }
}


//package ie.ucc.jonatanpoveda.backend;

//        import ie.ucc.jonatanpoveda.data.Complex64;

///**
// * Created by jonatanpoveda on 19/12/16.
// */
//
//public class InterpolatorCallable {
//    private int numberOfInterpolatedVectors;
//    private double deltaPhi, expectedPhaseChange;
//    private double[] timeDifferences;
//    private Complex64[] bandResult;
//
//    public InterpolatorCallable(double[] timeDifferences, double deltaPhi, int numberOfInterpolatedVectors) {
//        timeDifferences = timeDifferences;
//        deltaPhi = deltaPhi;
//        numberOfInterpolatedVectors = numberOfInterpolatedVectors;
//        bandResult = new Complex64[numberOfInterpolatedVectors];
//    }
//
//    public Complex64[] interpolate(double[] magnitudes, double[] phases, double expectedPhaseChange) {
//        expectedPhaseChange = expectedPhaseChange;
//
//        double magnitudeResult, deltaPhase;
//        for (int outputColumn = 0; outputColumn < numberOfInterpolatedVectors; outputColumn++) {
//            int inputColumn = outputColumn / 500;
//
//            /* Grab the two values from this band (they corresponds to different frames) */
//            double phase1 = phases[inputColumn];
//            double phase2 = phases[inputColumn + 1];
//            double magnitude1 = magnitudes[inputColumn];
//            double magnitude2 = magnitudes[inputColumn + 1];
//
//            /* Linear interpolation */
//            magnitudeResult = (1 - timeDifferences[outputColumn]) * magnitude1 + timeDifferences[outputColumn] * magnitude2;
//            /* Calculate expectedPhaseChange advance, fold the result to [-pi,pi) range */
//            deltaPhase = (phase2 - phase1 - deltaPhi) % Math.PI;
//            /* Return to rectangular form */
//            bandResult[outputColumn] = Complex64.polar2Rectangular(magnitudeResult, expectedPhaseChange);
//            /* Accumulate expectedPhaseChange, ready for next frame */
//            expectedPhaseChange += deltaPhi + deltaPhase;
//        }
//
//        return bandResult;
//    }
//
//    public double getPhase() {
//        return expectedPhaseChange;
//    }
//
//}
