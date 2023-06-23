package proj.research.colortools.db.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 数据库存储模型:线性模型
 */
@Entity(tableName = "linear_model")
public class LinearSeqModel {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.INTEGER)
    private int id;

    @ColumnInfo(name = "name", typeAffinity = ColumnInfo.TEXT)
    private String name;

    @ColumnInfo(name = "accuracy", typeAffinity = ColumnInfo.REAL, defaultValue = "0.0")
    private double accuracy;


    //将模型序列化为字符串(通过,分割) 分别为 r_weight,g_weight,b_weight,bias
    @ColumnInfo(name = "weights", typeAffinity = ColumnInfo.TEXT, defaultValue = "")
    private String weights;

    @ColumnInfo(name = "bias", typeAffinity = ColumnInfo.REAL, defaultValue = "0.0")
    private double bias;

    //训练/测试占比
    @ColumnInfo(name = "ratio", typeAffinity = ColumnInfo.REAL, defaultValue = "0.6")
    private double ratio;

    //容许误差
    @ColumnInfo(name = "error", typeAffinity = ColumnInfo.REAL, defaultValue = "0.1")
    private double error;


    //数据集,序列化为字符串 hex1:value1,hex2:value2...
    @ColumnInfo(name = "data", typeAffinity = ColumnInfo.TEXT, defaultValue = "")
    private String data;

    private boolean useR;
    private boolean useG;
    private boolean useB;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getWeights() {
        return weights;
    }

    public void setWeights(String weights) {
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    public boolean isUseR() {
        return useR;
    }

    public void setUseR(boolean useR) {
        this.useR = useR;
    }

    public boolean isUseG() {
        return useG;
    }

    public void setUseG(boolean useG) {
        this.useG = useG;
    }

    public boolean isUseB() {
        return useB;
    }

    public void setUseB(boolean useB) {
        this.useB = useB;
    }

    @Override
    public String toString() {
        return "LinearSeqModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", accuracy=" + accuracy +
                ", weights='" + weights + '\'' +
                ", bias=" + bias +
                ", ratio=" + ratio +
                ", error=" + error +
                ", data='" + data + '\'' +
                ", useR=" + useR +
                ", useG=" + useG +
                ", useB=" + useB +
                '}';
    }
}
