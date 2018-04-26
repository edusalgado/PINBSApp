package ie.ucc.salgadoe.pinbsapp.data;

/**
 * Container for interpolating a band of frequencies
 */
public class Job {
    private int numberOfInterpolatedVectors;
    private double deltaPhi;
    private double acumulatedPhase;
    private double[] bandMagnitudes;
    private double[] bandPhases;
    private double[] timeDifferences;
    private int ratio;

    public Job(double[] timeDifferences, double deltaPhi, int numberOfInterpolatedVectors,
               double[] bandMagnitudes, double[] bandPhases, double expectedPhaseChange) {
        this.setNumberOfInterpolatedVectors(numberOfInterpolatedVectors);
        this.setDeltaPhi(deltaPhi);
        this.setAcumulatedPhase(expectedPhaseChange);
        this.setBandMagnitudes(bandMagnitudes);
        this.setBandPhases(bandPhases);
        this.setTimeDifferences(timeDifferences);
    }

    /* Getters and setters */
    public int getNumberOfInterpolatedVectors() {
        return numberOfInterpolatedVectors;
    }

    public void setNumberOfInterpolatedVectors(int numberOfInterpolatedVectors) {
        this.numberOfInterpolatedVectors = numberOfInterpolatedVectors;
    }

    public double getDeltaPhi() {
        return deltaPhi;
    }

    public void setDeltaPhi(double deltaPhi) {
        this.deltaPhi = deltaPhi;
    }

    public double getAcumulatedPhase() {
        return acumulatedPhase;
    }

    public void setAcumulatedPhase(double acumulatedPhase) {
        this.acumulatedPhase = acumulatedPhase;
    }

    public double[] getBandMagnitudes() {
        return bandMagnitudes;
    }

    public void setBandMagnitudes(double[] bandMagnitudes) {
        this.bandMagnitudes = bandMagnitudes;
    }

    public double[] getBandPhases() {
        return bandPhases;
    }

    public void setBandPhases(double[] bandPhases) {
        this.bandPhases = bandPhases;
    }

    public double[] getTimeDifferences() {
        return timeDifferences;
    }

    public void setTimeDifferences(double[] timeDifferences) {
        this.timeDifferences = timeDifferences;
    }

    public int getRatio() {
        return 500;
    }
}
