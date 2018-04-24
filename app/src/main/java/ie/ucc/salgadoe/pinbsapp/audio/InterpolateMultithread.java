package ie.ucc.salgadoe.pinbsapp.audio;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ie.ucc.salgadoe.pinbsapp.data.Complex64Matrix;
import ie.ucc.salgadoe.pinbsapp.data.InterpolatorResult;
import ie.ucc.salgadoe.pinbsapp.data.Job;



/**
 * Distributes a job over various threads and join their results.
 * <p>
 * The interpolation of a matrix' magnitudes and its phase transformation is considered a job.
 * </p>
 */
public class InterpolateMultithread {
    private static final String TAG = "InterpolateMultithread";
    private ArrayList<InterpolatorCallable> workers;
    private ExecutorService executor;
    private int mNumberOfInterpolatedVectors;
    private double[] mExpectedPhaseChange;
    private int mNumberOfThreads;

    public InterpolateMultithread(int numberOfThreads) {
        mNumberOfThreads = numberOfThreads;
        workers = new ArrayList<>();
        // Launch a reusable pool of threads
        executor = Executors.newFixedThreadPool(mNumberOfThreads);
    }

    /**
     * Gets the input data, distribute it by bands and prepare them as tasks / jobs
     */
    public void addJob(ArrayList<double[]> magnitudeArray, ArrayList<double[]> phaseArray, double[] timeDifferences, double[] deltaPhi, int numberOfInterpolatedVectors, double[] expectedPhaseChange) {
        mNumberOfInterpolatedVectors = numberOfInterpolatedVectors;
        mExpectedPhaseChange = expectedPhaseChange;

        // Split data over workers (each frequency band to a different worker)
        workers.clear();
        for (int band = 0; band < magnitudeArray.size(); band++) {
            double[] magnitudes = magnitudeArray.get(band);
            double[] phases = phaseArray.get(band);
            Job job = new Job(timeDifferences, deltaPhi[band], numberOfInterpolatedVectors, magnitudes, phases, expectedPhaseChange[band]);
            workers.add(band, new InterpolatorCallable(job));
        }
    }

    /**
     * Runs a job
     *
     * @return : result of the job
     */
    public Complex64Matrix getResult() {
        ArrayList<Future<InterpolatorResult>> results = new ArrayList<>();
        Complex64Matrix matrix = new Complex64Matrix(mNumberOfInterpolatedVectors, mExpectedPhaseChange.length);

        /* This is here because of shutting down executor every time (if not, insert it to the constructor) */
//        executor = Executors.newFixedThreadPool(mNumberOfThreads);

        /* Execute job in multi-threading */
        for (int band = 0; band < mExpectedPhaseChange.length; band++) {
            results.add(band, executor.submit(workers.get(band)));
        }

        /* Start shutting down */
//        executor.shutdown();

        try {
            /* Retrieve results (blocking operation) */
            for (int band = 0; band < mExpectedPhaseChange.length; band++) {
                InterpolatorResult result = results.get(band).get();
                matrix.setBand(result.getBand(), band);
                mExpectedPhaseChange[band] = result.getPhase();
            }

        } catch (InterruptedException e) {
            Log.d(TAG, "getResult: Thread interrupted");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d(TAG, "getResult: Exception received doing the interpolation");
            e.printStackTrace();
        }

        /* Ensure shutdown of threads */
//        if (!executor.isTerminated()) {
//            Log.d(TAG, "getResult: Shutting down all the threads");
//            executor.shutdownNow();
//        }

        return matrix;
    }

    /**
     * Returns the last expected phase change to use it in future callings
     *
     * @return : the resulting expected phase change
     */
    public double[] getNextExpectedPhase() {
        return mExpectedPhaseChange;
    }

}