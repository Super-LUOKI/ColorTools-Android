package proj.research.colortools.bean;

import java.util.List;

public class TaskData {
    public List<List<Double>> feats;
    public List<Double> targets;
    public double trainRatio;
    public double allowError;

    public List<List<Double>> getFeats() {
        return feats;
    }

    public void setFeats(List<List<Double>> feats) {
        this.feats = feats;
    }

    public List<Double> getTargets() {
        return targets;
    }

    public void setTargets(List<Double> targets) {
        this.targets = targets;
    }

    public double getTrainRatio() {
        return trainRatio;
    }

    public void setTrainRatio(double trainRatio) {
        this.trainRatio = trainRatio;
    }

    public double getAllowError() {
        return allowError;
    }

    public void setAllowError(double allowError) {
        this.allowError = allowError;
    }

    @Override
    public String toString() {
        return "TaskData{" +
                "feats=" + feats +
                ", targets=" + targets +
                ", trainRatio=" + trainRatio +
                ", allowError=" + allowError +
                '}';
    }
}
