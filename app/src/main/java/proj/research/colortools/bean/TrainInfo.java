package proj.research.colortools.bean;

public class TrainInfo {
    private String id;
    private double rW;
    private double gW;
    private double bW;
    private double bias;
    private double accuracy;
    private boolean done;
    private int epochs;
    private int progress;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getrW() {
        return rW;
    }

    public void setrW(double rW) {
        this.rW = rW;
    }

    public double getgW() {
        return gW;
    }

    public void setgW(double gW) {
        this.gW = gW;
    }

    public double getbW() {
        return bW;
    }

    public void setbW(double bW) {
        this.bW = bW;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }


    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public int getEpochs() {
        return epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public String toString() {
        return "TrainInfo{" +
                "id='" + id + '\'' +
                ", rW=" + rW +
                ", gW=" + gW +
                ", bW=" + bW +
                ", bias=" + bias +
                ", accuracy=" + accuracy +
                ", done=" + done +
                ", epochs=" + epochs +
                ", progress=" + progress +
                '}';
    }
}
