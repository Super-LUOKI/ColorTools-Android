package proj.research.colortools.bean;

import java.util.Arrays;
import java.util.Map;

/**
 * 线性模型类
 */
public class LinearModel {
    private String name;
    private double accuracy;
    private double[] weights;
    private double bias;
    //训练/测试占比
    private double ratio;
    //容许误差
    private double error;

    //数据集[[hex, value]...]
    private Map<String, Double> data;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public Map<String, Double> getData() {
        return data;
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "LinearModel{" +
                "name='" + name + '\'' +
                ", accuracy=" + accuracy +
                ", weights=" + Arrays.toString(weights) +
                ", bias=" + bias +
                ", ratio=" + ratio +
                ", error=" + error +
                ", data=" + data +
                '}';
    }
}
