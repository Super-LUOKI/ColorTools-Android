package proj.research.colortools.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import proj.research.colortools.bean.LinearModel;
import proj.research.colortools.db.LocalDatabase;
import proj.research.colortools.db.model.LinearSeqModel;

public class Local {

    /**
     * 将LinearModel转换为LinearSeqModel
     * @param seqModel
     * @return
     */
    public static LinearModel seqModelToModel(LinearSeqModel seqModel){
        LinearModel model = new LinearModel();
        model.setName(seqModel.getName());
        model.setAccuracy(seqModel.getAccuracy());
        model.setBias(seqModel.getBias());
        model.setRatio(seqModel.getRatio());
        model.setError(seqModel.getError());
        String[] weightsStr = seqModel.getWeights().split(",");
        double[] weights = new double[weightsStr.length];
        for (int i = 0; i < weightsStr.length; i++) {
            weights[i] = Double.parseDouble(weightsStr[i]);
        }
        model.setWeights(weights);
        return model;
    }

    //请求权限请求码
    public static int REQUEST_CODE_ASK_PERMISSIONS = 801;
    private static final String TAG = "Local";
    public static final String BASE_URL = "";

    public static void showDialog(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", null);
        builder.show();
    }


    /**
     * 检查权限状态
     *
     * @param activity
     * @param permissions
     * @return 已拥有权限返回null，否则返回未拥有的权限
     */
    public static List<String> checkPermission(Activity activity, String[] permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            //定义集合存储缺少的权限
            List<String> lackedPermission = new ArrayList<String>();
            //检查权限
            for (String permis : permissions) {
                if (!(activity.checkSelfPermission(permis) == PackageManager.PERMISSION_GRANTED)) {
                    lackedPermission.add(permis);
                }
            }

            // 权限都已经有了，那么直接调用SDK
            if (lackedPermission.size() != 0) {
                return lackedPermission;
            }
        }
        return null;
    }

    /**
     * 申请权限
     *
     * @param activity
     * @param lackedPermission
     */
    public static void requestPermission(Activity activity, List<String> lackedPermission) {
        if (Build.VERSION.SDK_INT >= 23) {

            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            activity.requestPermissions(requestPermissions, REQUEST_CODE_ASK_PERMISSIONS);

        }
    }


    /**
     * 发送POST请求
     *
     * @param url  请求地址（不需要带BaseURL）
     * @param data json字符串
     * @return
     */
    public static String post(String url, String data) {
        try {
            URL url1 = new URL(BASE_URL.length() > 0 ? BASE_URL + url : url);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.getOutputStream().write(data.getBytes());
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                byte[] bytes = new byte[1024];
                int len = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((len = is.read(bytes)) != -1) {
                    baos.write(bytes, 0, len);
                }
                String result = baos.toString();
                //Log.e(TAG, "post: " + result);
                return result;
            } else {
                Log.e(TAG, "请求失败，post: " + code);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 发送POST请求
     * @param baseURrl 基础URL
     * @param url 请求地址（不需要带BaseURL）
     * @param data json字符串
     * @return
     */
    public static String post(String baseURrl,String url, String data) {
        try {
            URL url1 = new URL(baseURrl + url);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.getOutputStream().write(data.getBytes());
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                byte[] bytes = new byte[1024];
                int len = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((len = is.read(bytes)) != -1) {
                    baos.write(bytes, 0, len);
                }
                String result = baos.toString();
                //Log.e(TAG, "post: " + result);
                return result;
            } else {
                Log.e(TAG, "请求失败，post: " + code);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    //测试用途:算数表达式接口
    public interface AlgFunc {
        double getY(double... params);
    }

    //测试用途
    public static List<Double[]> getThreeXData(int dataSize, AlgFunc func) {
        List<Double[]> ls = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < dataSize; i++) {

            double x1 = (double) random.nextInt(100) / 100;
            double x2 = (double) random.nextInt(100) / 100;
            double x3 = (double) random.nextInt(100) / 100;
            double y = func.getY(x1, x2, x3);
            Double[] d = {x1, x2, x3, y};
            ls.add(d);

        }
        return ls;
    }

    /**
     * 从SharedPreference中读取配置文件
     * @param key
     * @return
     */
    public static String getConfig(Context context, String key, String defValue){
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getString(key,defValue);
    }

    /**
     * 保存配置文件到SharedPreference
     * @param key
     * @param value
     */
    public static void saveConfig(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static boolean isChineseLanguage(){
        return Locale.getDefault().getLanguage().equals("zh");
    }

    private static final String AES_MODEL_KEY = "model";
    /**
     * 导出单个模型
     * @param context 上下文
     * @param model 模型
     * @return
     */
    public static boolean exportModel(Context context, LinearSeqModel model) {
        if(model == null) return false;
        //使用FastJson将Model序列化
        String jsonString = JSON.toJSONString(model);
        JSONObject jsonObject = JSON.parseObject(jsonString);
        //删除id
        jsonObject.remove("id");
        jsonString = jsonObject.toJSONString();
        //加密并调用系统分享
        try {
            String encrypted = encryptStr(jsonString, AES_MODEL_KEY);
            //保存到本地
            File file = new File(context.getExternalFilesDir(null), "color.model");
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(encrypted.getBytes());
            outputStream.close();

            // 分享保存的文件
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", file);
            Log.e(TAG, "uri: " + uri.toString());
            Log.e(TAG, "encrypted: " + encrypted);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "颜色模型分享"));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to export model.", e);
            return false;
        }
    }

    /**
     * 导入单个模型
     * @param context
     * @param in 输入流
     * @return
     */
    public static LinearSeqModel importModel(Context context, InputStream in) {
       //将in转成文本
        String jsonString = null;
        try {
            jsonString = new BufferedReader(new InputStreamReader(in))
                    .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to read input stream.", e);
            return null;
        }
        //解密
        try {
            jsonString = decryptStr(jsonString, AES_MODEL_KEY);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt model.", e);
            return null;
        }
        //使用FastJson将文本反序列化为Model
        LinearSeqModel model = JSON.parseObject(jsonString, LinearSeqModel.class);
        //保存到数据库
        if(model != null) {
            LocalDatabase.getInstance(context).getLinearModelDao().insert(model);
            return model;
        }
        return null;
    }

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public static String encryptStr(String str, String key) throws Exception {
        SecretKeySpec secretKey = generateSecretKey(key);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encryptedBytes);
    }

    public static String decryptStr(String str, String key) throws Exception {
        SecretKeySpec secretKey = generateSecretKey(key);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = hexToBytes(str);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static SecretKeySpec generateSecretKey(String key) throws NoSuchAlgorithmException {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 16);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
