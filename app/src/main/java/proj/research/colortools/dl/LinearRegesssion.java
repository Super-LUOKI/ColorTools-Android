package proj.research.colortools.dl;

import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

import proj.research.colortools.util.Local;

public class LinearRegesssion {
    private static final String TAG = "LinearRegesssion";
    public static int numInput = 3;
    public static int numOutput = 1;
    public static long seed = 12345;
    public static double learningRate = 0.01;
    public static int batchSize = 5;
    public static int epoches = 2000;





    /**
     * 训练模型
     *
     * @param feats   [[r1, g1, b1], ...]
     * @param targets [val1, val2, ...]
     * @return [w1, w2, w3]
     */
    public static Double[] train(Double[][] feats, Double[] targets, Progress progress) {

        try {
            ////数据转Json fastjson
            //JSONObject jsonObject = new JSONObject();
            //JSONArray featsJson = new JSONArray();
            //for (Double[] feat : feats) {
            //    featsJson.put(toJsonArray(feat));
            //}
            //jsonObject.put("feats", featsJson);
            //jsonObject.put("targets", toJsonArray(targets));
            //String data = jsonObject.toString();
            ////Log.e(TAG, "train: " + data);
            //发送请求
            //URL url = new URL(Local.BASE_URL + "/dl/train");
            //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setRequestMethod("POST");
            //conn.setDoOutput(true);
            //conn.setDoInput(true);
            //conn.setRequestProperty("Content-Type", "application/json");
            //conn.setRequestProperty("Charset", "UTF-8");
            //conn.getOutputStream().write(data.getBytes());
            //conn.getOutputStream().flush();
            //conn.getOutputStream().close();
            ////获取响应
            //int code = conn.getResponseCode();
            //if (code == 200) {
            //    String result = Local.readStream(conn.getInputStream());
            //    //Log.e(TAG, "train: " + result);
            //    JSONObject resultJson = new JSONObject(result);
            //    JSONArray weights = resultJson.getJSONArray("weights");
            //    Double[] rst = new Double[weights.length()];
            //    for (int i = 0; i < weights.length(); i++) {
            //        rst[i] = weights.getDouble(i);
            //    }
            //    return rst;
            //} else {
            //    Log.e(TAG, "train: " + code);
            //}

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }


        return null;
    }

    /**
     * 验证模型的准确率
     *
     * @param feats   [[r1, g1, b1], ...]
     * @param targets [val1, val2, ...]
     * @return 准确率
     */
    public static Double test(Double[][] feats, Double[] targets, Double allowError) {
        //double[] preditX = new double[feats.length * 3];
        //int len = 0;
        //for (Double[] feat : feats) {
        //    for (Double f : feat) {
        //        preditX[len++] = f;
        //    }
        //}
        //INDArray input = Nd4j.create(preditX, new int[]{feats.length, 3});
        //INDArray output = model.output(input);
        //double[] preditY = output.toDoubleVector();
        //int right = 0;
        //for (int i = 0; i < preditY.length; i++) {
        //    if (Math.abs(preditY[i] - targets[i]) < allowError) {
        //        right++;
        //    }
        //}
        //return right * 1.0 / preditY.length;
        return 0.0;
    }

    public interface Progress {
        void onProgress(int epoch, double total);
    }


}
