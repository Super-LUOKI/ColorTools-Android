package proj.research.colortools;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import proj.research.colortools.bean.LinearModel;
import proj.research.colortools.bean.ResInfo;
import proj.research.colortools.bean.TaskData;
import proj.research.colortools.bean.TrainInfo;
import proj.research.colortools.db.LocalDatabase;
import proj.research.colortools.db.dao.ILinearModelDao;
import proj.research.colortools.db.model.LinearSeqModel;
import proj.research.colortools.dl.LinearRegesssion;
import proj.research.colortools.util.Local;

public class ModelDetailActivity extends AppCompatActivity {

    private static final String TAG = "ModelDetailActivity";
    private ILinearModelDao mModelDao;
    private LocalDatabase mDB;
    private LinearSeqModel mSeqModel;
    private LinearModel mModel;
    private int mModelId;
    private EditText mModelNameEd;
    private EditText mTrainRatioiEd;
    private TextView mAccuracyTv;
    private TextView mDataSetCntTv;
    private TextView mFitResultTv;
    private CheckBox mRChannelCb;
    private CheckBox mGChannelCb;
    private CheckBox mBChannelCb;
    private EditText mErrorEd;
    private RecyclerView mDataList;
    private FloatingActionButton mAddBtn;
    private DataListAdapter mDataListAdapter;
    private EditText mAIServerEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_detail);
        //获取传入的模型id
        mModelId = getIntent().getIntExtra("model_id", -1);
        Log.e(TAG, "onCreate: " + mModelId);
        if (mModelId == -1) {
            //如果没有传入模型id，说明是新建模型，直接返回
            finish();
        }
        initData();
        initView();
        setViewData();
        initAdapterAndListener();
    }


    private void initData() {
        //根据id获取模型信息
        mDB = LocalDatabase.getInstance(this);
        mModelDao = mDB.getLinearModelDao();
        loadModel();
    }

    private void loadModel() {
        mSeqModel = mModelDao.query(mModelId);
        Log.e(TAG, "loadModel: " + mSeqModel);
        //将存储模型转换为使用模型
        mModel = new LinearModel();
        mModel.setName(mSeqModel.getName());
        mModel.setAccuracy(mSeqModel.getAccuracy());

        //反字符串序列化获得数据集列表
        String dataSetStr = mSeqModel.getData();
        if (dataSetStr == null || dataSetStr.length() == 0) {
            mModel.setData(new HashMap<String, Double>());
        } else {
            String[] dataSet = dataSetStr.split(",");
            Map<String, Double> dataMap = new HashMap<>();
            for (String s : dataSet) {
                String[] data = s.split(":");
                dataMap.put(data[0], Double.parseDouble(data[1]));
            }
            mModel.setData(dataMap);
        }

        mModel.setBias(mSeqModel.getBias());
        mModel.setRatio(mSeqModel.getRatio());
        mModel.setError(mSeqModel.getError());

        //反序列化参数
        String paramStr = mSeqModel.getWeights();
        if (paramStr == null || paramStr.length() == 0) {
            mModel.setWeights(new double[]{0.0, 0.0, 0.0});
        } else {
            String[] params = paramStr.split(",");
            double[] weights = new double[params.length];
            for (int i = 0; i < params.length; i++) {
                weights[i] = Double.parseDouble(params[i]);
            }
            mModel.setWeights(weights);
        }

        Log.e(TAG, "loadModel:" + mModel.toString());
    }

    private void initView() {
        //初始化界面
        //将模型名称设置为页面标题
        setTitle(mModel.getName());
        //显示返回按钮并设置返回事件
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mModelNameEd = findViewById(R.id.ed_model_name);
        mAccuracyTv = findViewById(R.id.tv_accuracy);
        mDataSetCntTv = findViewById(R.id.tv_dataset_count);
        mTrainRatioiEd = findViewById(R.id.ed_train_ratio);
        mRChannelCb = findViewById(R.id.ckb_r);
        mGChannelCb = findViewById(R.id.ckb_g);
        mBChannelCb = findViewById(R.id.ckb_b);
        mFitResultTv = findViewById(R.id.tv_fit_result);
        mDataList = findViewById(R.id.data_list);
        mAddBtn = findViewById(R.id.data_add);
        mErrorEd = findViewById(R.id.ed_error);
        mAIServerEdit = findViewById(R.id.ed_ai_server);


    }

    /**
     * 设置页面显示的数据
     */
    private void setViewData() {
        //从SharedPreference中读取AI Server地址并写入
        String aiServer = Local.getConfig(this, "ai_server", null);
        if (aiServer != null) {
            mAIServerEdit.setText(aiServer);
        }

        mModelNameEd.setText(mModel.getName());
        mAccuracyTv.setText(String.format("%.2f%%", mModel.getAccuracy() * 100));
        mDataSetCntTv.setText(String.valueOf(mModel.getData().size()));
        mTrainRatioiEd.setText(String.valueOf(mModel.getRatio()));
        mErrorEd.setText(String.valueOf(mModel.getError()));
        //通过判断weights中某一项不为0来判断是否使用该通道
        //由于天然存在的浮点精度问题，此处使用区间判断是否为0
        double testRange = 0.00000001;
        double rW = mModel.getWeights()[0];
        double gW = mModel.getWeights()[1];
        double bW = mModel.getWeights()[2];
        boolean useRChannel = !(rW > -testRange && rW < testRange);
        boolean useGChannel = !(gW > -testRange && gW < testRange);
        boolean useBChannel = !(bW > -testRange && bW < testRange);
        mRChannelCb.setChecked(useRChannel);
        mGChannelCb.setChecked(useGChannel);
        mBChannelCb.setChecked(useBChannel);
        if (!(useRChannel || useGChannel || useBChannel)) {
            mFitResultTv.setText(Local.isChineseLanguage() ? "模型未训练" : "Model not trained");
        } else {
            //将拟合结果转换为字符串表示的函数
            String fitResult = "y = ";
            if (useRChannel) {
                fitResult += String.format("%.4f", rW) + " * R + ";
            } else {
                //不允许更改选项值
                mRChannelCb.setEnabled(false);
            }
            if (useGChannel) {
                fitResult += String.format("%.4f", gW) + " * G + ";
            } else {
                mGChannelCb.setEnabled(false);
            }
            if (useBChannel) {
                fitResult += String.format("%.4f", bW) + " * B + ";
            } else {
                mBChannelCb.setEnabled(false);
            }
            fitResult += String.format("%.4f", mModel.getBias());
            mFitResultTv.setText(fitResult);
        }
    }


    private void initAdapterAndListener() {
        //设置RecyclerView
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mDataList.setLayoutManager(manager);
        //设置适配器
        List<String> hexList = new ArrayList<>(mModel.getData().keySet());
        mDataListAdapter = new DataListAdapter(hexList);
        mDataList.setAdapter(mDataListAdapter);

        //添加训练集按钮
        mAddBtn.setOnClickListener(new AddDataListener());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case 0:
                //训练并保存
                trainAndSave();
                break;
            case 1:
                //删除模型
                deleteModel();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //训练并保存模型
    private void trainAndSave() {
        //模型名称
        String name = mModelNameEd.getText().toString().trim();
        //取色通道信息
        boolean useRChannel = mRChannelCb.isChecked();
        boolean useGChannel = mGChannelCb.isChecked();
        boolean useBChannel = mBChannelCb.isChecked();
        //训练集占比
        String ratio = mTrainRatioiEd.getText().toString().trim();
        double ratioDouble = Double.parseDouble(ratio);
        //容许误差
        String error = mErrorEd.getText().toString().trim();
        double errorDouble = Double.parseDouble(error);

        //判断模型名称是否合适
        if (!name.equals(mModel.getName())) {
            //检测是否已存在同名模型
            if (mModelDao.queryCountByName(name) > 1) {
                //已存在同名模型
                if (Local.isChineseLanguage()) {
                    Local.showDialog(this, "提示", "模型名称已存在, 请重新输入模型名称");
                } else {
                    Local.showDialog(this, "Tips", "The model name already exists, please re-enter the model name");
                }
                return;
            }
            if (name.length() == 0) {
                if (Local.isChineseLanguage()) {
                    Local.showDialog(this, "提示", "模型名称不能为空");
                } else {
                    Local.showDialog(this, "Tips", "The model name cannot be empty");
                }
                return;
            }
        }
        //判断是否已经选择了取色通道
        if (!(useRChannel || useGChannel || useBChannel)) {
            if (Local.isChineseLanguage()) {
                Local.showDialog(this, "提示", "请至少选择一个取色通道");
            } else {
                Local.showDialog(this, "Tips", "Please select at least one color channel");
            }
            return;
        }

        //判断是否选择了容许误差
        if (error.length() <= 0 || errorDouble <= 0) {
            if (Local.isChineseLanguage()) {
                Local.showDialog(this, "提示", "容许误差必须设置且大于0");
            } else {
                Local.showDialog(this, "Tips", "The allowable error must be set and greater than 0");
            }
            return;
        }

        int dataCnt = mModel.getData().size();
        Log.e(TAG, "trainAndSave: 数据集数量：" + dataCnt);
        //判断是否存在数据集
        if (dataCnt < 10) {
            if (Local.isChineseLanguage()) {
                Local.showDialog(this, "提示", "至少需要10个数据集");
            } else {
                Local.showDialog(this, "Tips", "At least 10 data sets are required");
            }
            return;
        }
        //判断是是否设置了训练集占比
        if (ratio.length() <= 0 || ratioDouble <= 0 || ratioDouble >= 1) {
            if (Local.isChineseLanguage()) {
                Local.showDialog(this, "提示", "训练集占比必须设置且大于0小于1");
            } else {
                Local.showDialog(this, "Tips", "The training set ratio must be set and greater than 0 and less than 1");
            }
            return;
        }
        //判断训练集占比是否合适(训练集至少包含2个数据，测试集至少包含一个数据)
        int trainCnt = (int) (dataCnt * ratioDouble);
        int testCnt = dataCnt - trainCnt;
        if (trainCnt < 2 || testCnt < 1) {
            if (Local.isChineseLanguage()) {
                Local.showDialog(this, "提示", "训练数据量：" + trainCnt + "\n验证数据量：" + testCnt + "\n训练集占比过大或过小，请重新输入。");
            } else {
                Local.showDialog(this, "Tips", "Training data: " + trainCnt + "\nValidation data: " + testCnt + "\nThe training set ratio is too large or too small, please re-enter it.");
            }
            return;
        }
        //开始训练数据
        doTrain(ratioDouble, errorDouble);
        mModelDao.updateBaseInfo(mSeqModel.getId(), name, ratioDouble, errorDouble, useRChannel, useGChannel, useBChannel);
    }

    //删除模型
    private void deleteModel() {
        mModelDao.delete(mSeqModel.getId());
        if (Local.isChineseLanguage()) {
            toast("删除成功");
        } else {
            toast("Delete success");
        }
        finish();
    }

    private void doTrain(double trainRatio, double error) {
        Map<String, Double> dataMap = mModel.getData();
        List<String> hexList = new ArrayList<>(dataMap.keySet());

        List<List<Double>> input = new ArrayList<>();
        List<Double> output = new ArrayList<>();

        for (String hex : hexList) {
            List<Double> rgb = new ArrayList<>();
            int hexColor = Color.parseColor(hex);
            rgb.add((double) Color.red(hexColor));
            rgb.add((double) Color.green(hexColor));
            rgb.add((double) Color.blue(hexColor));
            input.add(rgb);
            output.add(dataMap.get(hex));
        }

        //List<Double[]> data = Local.getThreeXData(20, (x) -> (3.0 * x[0] + 4.0 * x[1] + 6.0 * x[2]));
        //List<List<Double>> input = new ArrayList<>();
        //List<Double> output = new ArrayList<>();
        //
        //for (Double[] d : data) {
        //    List<Double> perInput = new ArrayList<>();
        //    perInput.add(d[0]);
        //    perInput.add(d[1]);
        //    perInput.add(d[2]);
        //    input.add(perInput);
        //    output.add(d[3]);
        //}

        TrainAsyncTask task = new TrainAsyncTask();
        TaskData taskData = new TaskData();
        taskData.feats = input;
        taskData.targets = output;
        taskData.trainRatio = trainRatio;
        taskData.allowError = error;
        task.execute(taskData);


    }

    /**
     * 神经网络训练异步任务
     */
    private class TrainAsyncTask extends AsyncTask<TaskData, Integer, TrainInfo> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            int totalProgress = 100;
            //设置进度条弹框
            mProgressDialog = new ProgressDialog(ModelDetailActivity.this);
            if (Local.isChineseLanguage()) {
                mProgressDialog.setTitle("训练中");
                mProgressDialog.setMessage("正在训练中，请稍后...");
            } else {
                mProgressDialog.setTitle("Training");
                mProgressDialog.setMessage("Training, please wait...");
            }
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(totalProgress);
            mProgressDialog.show();
        }

        @Override
        protected TrainInfo doInBackground(TaskData... lists) {
            String base_url = mAIServerEdit.getText().toString().trim();
            //如果为空则返回null
            if (base_url.length() == 0) {
                return null;
            }
            TaskData taskData = lists[0];
            TrainInfo trainInfo = null;
            //数据对象转json并发起请求
            String resJsonStr = Local.post(base_url, "/dl/train", JSON.toJSONString(taskData));
            if (resJsonStr == null) {
                return null;
            }
            String trainId = (String) JSON.parseObject(resJsonStr, ResInfo.class).getMsg();
            try {
                while (true) {
                    //每隔2s查询一次训练情况
                    Thread.sleep(2000);
                    String checkJson = Local.post(base_url, "/dl/check", "{\"trainingId\":\"" + trainId + "\"}");
                    if (resJsonStr == null) {
                        return null;
                    }
                    JSONObject jo = (JSONObject) JSON.parseObject(checkJson, ResInfo.class).getMsg();
                    Log.e(TAG, "doInBackground: " + jo.toString());
                    trainInfo = jo.toJavaObject(TrainInfo.class);
                    publishProgress(trainInfo.getProgress(), trainInfo.getEpochs());
                    Log.e(TAG, trainInfo.toString());
                    if (trainInfo.isDone()) {
                        Log.e(TAG, "over");
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: " + e.toString());
                e.printStackTrace();
            }


            return trainInfo;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = (int) ((double) values[0] / values[1] * 100);
            mProgressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(TrainInfo trainInfo) {
            super.onPostExecute(trainInfo);
            mProgressDialog.dismiss();
            if (trainInfo != null) {
                Log.e(TAG, "onPostExecute: " + trainInfo.toString());
                System.out.println(trainInfo);
                //打印结果
                String weights = trainInfo.getrW() + "," + trainInfo.getgW() + "," + trainInfo.getbW();
                mModelDao.updateTrainedInfo(mSeqModel.getId(), weights, trainInfo.getBias(), trainInfo.getAccuracy());
                //将AI Server地址写入SharedPreference
                Local.saveConfig(ModelDetailActivity.this, "ai_server", mAIServerEdit.getText().toString().trim());

                //重新加载数据
                loadModel();
                setViewData();
            } else {
                if (Local.isChineseLanguage()) {
                    Local.showDialog(ModelDetailActivity.this, "提示", "和服务器通信失败");
                } else {
                    Local.showDialog(ModelDetailActivity.this, "Tips", "Failed to communicate with the server");
                }
            }


        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressDialog.dismiss();
        }

        public <T> T[] toArray(List<T> t) {
            T[] array = (T[]) Array.newInstance(t.get(0).getClass(), t.size());
            for (int i = 0; i < t.size(); i++) {
                array[i] = t.get(i);
            }
            return array;
        }

        private JSONArray toJsonArray(Double[] doubles) throws JSONException {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < doubles.length; i++) {
                jsonArray.put(doubles[i]);
            }
            return jsonArray;
        }
    }

    private class DataListAdapter extends RecyclerView.Adapter<InnerHolder> {

        private List<String> mHexDataList;

        public DataListAdapter(List<String> hexList) {
            mHexDataList = hexList;
        }

        public void updateDataSet(List<String> hexList) {
            mHexDataList = hexList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
            return new InnerHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull InnerHolder holder, int position) {
            holder.setData(mHexDataList.get(position));
            //设置删除图像按钮点击事件
            holder.mDeleteIv.setOnClickListener(v -> {
                boolean isChines = Local.isChineseLanguage();
                //弹窗提示确认删除
                AlertDialog.Builder builder = new AlertDialog.Builder(ModelDetailActivity.this);
                if (isChines) {
                    builder.setTitle("删除数据");
                    builder.setMessage("确认删除该数据吗？");
                } else {
                    builder.setTitle("Delete Data");
                    builder.setMessage("Are you sure to delete this data?");
                }
                builder.setPositiveButton(isChines ? "确认" : "Confirm", (dialog, which) -> {
                    //从数据库中删除数据
                    deleteDataSet(mHexDataList.get(position));
                    updateDataSet(new ArrayList<>(mModel.getData().keySet()));
                });
                builder.setNegativeButton(isChines ? "取消" : "Cancel", null);
                builder.show();

            });
        }

        /**
         * 从数据库中删除数据（将删除后的替换删除前的,以实现删除的效果）
         *
         * @param hex 待删除的颜色值对应的数据
         */
        private void deleteDataSet(String hex) {
            Map<String, Double> data = mModel.getData();
            data.remove(hex);
            //新数据序列化为字符串
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                sb.append(entry.getKey() + ":" + entry.getValue() + ",");
            }
            if (sb.length() > 0) {
                //删除最后一个逗号
                sb.deleteCharAt(sb.length() - 1);
            }

            //更新数据库
            mModelDao.updateData(mSeqModel.getId(), sb.toString());
            loadModel();
        }

        @Override
        public int getItemCount() {
            return mHexDataList.size();
        }
    }

    private class InnerHolder extends RecyclerView.ViewHolder {
        public ImageView mColorIv;
        public TextView mHexTv;
        public TextView mRgbTv;
        public TextView mPotencyTv;
        public ImageView mDeleteIv;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            mColorIv = itemView.findViewById(R.id.data_color_preview);
            mHexTv = itemView.findViewById(R.id.data_hex);
            mRgbTv = itemView.findViewById(R.id.data_rgb);
            mPotencyTv = itemView.findViewById(R.id.data_potency);
            mDeleteIv = itemView.findViewById(R.id.data_delete);

        }

        /**
         * 绑定数据
         *
         * @param hexStr 例如：#666666
         */
        public void setData(String hexStr) {
            Log.e(TAG, "setData: " + hexStr);
            Double petency = mModel.getData().get(hexStr);
            if (petency == null) {
                return;
            }
            int color = Color.parseColor(hexStr);
            mColorIv.setBackgroundColor(color);
            if (Local.isChineseLanguage()) {
                mHexTv.setText("Hex: " + hexStr);
                mRgbTv.setText("RGB: " + Color.red(color) + ", " + Color.green(color) + ", " + Color.blue(color));
                mPotencyTv.setText("浓度值: " + petency);
            } else {
                mHexTv.setText("Hex: " + hexStr);
                mRgbTv.setText("RGB: " + Color.red(color) + ", " + Color.green(color) + ", " + Color.blue(color));
                mPotencyTv.setText("Potency: " + petency);
            }

        }
    }

    private class AddDataListener implements View.OnClickListener {
        private View mRootView;
        private EditText mRChannelEd;
        private EditText mGChannelEd;
        private EditText mBChannelEd;
        private EditText mPotencyChannelEd;

        public AddDataListener() {
            mRootView = LayoutInflater.from(ModelDetailActivity.this).inflate(R.layout.dialog_data, null, false);
            mRChannelEd = mRootView.findViewById(R.id.ed_r_channel);
            mGChannelEd = mRootView.findViewById(R.id.ed_g_channel);
            mBChannelEd = mRootView.findViewById(R.id.ed_b_channel);
            mPotencyChannelEd = mRootView.findViewById(R.id.ed_potency);
        }

        @Override
        public void onClick(View v) {
            //添加数据弹窗
            AlertDialog.Builder builder = new AlertDialog.Builder(ModelDetailActivity.this);
            boolean isChinese = Local.isChineseLanguage();
            if (isChinese) {
                builder.setTitle("添加数据");
            } else {
                builder.setTitle("Add Data");
            }
            builder.setView(mRootView);

            builder.setPositiveButton(isChinese ? "确认添加" : "Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmAdd();
                    ((ViewGroup) mRootView.getParent()).removeAllViews();
                }
            });
            builder.setNegativeButton(isChinese ? "取消" : "Cancel", (dialog, which) -> {
                //获取mRootView的父布局，清空其子View
                ((ViewGroup) mRootView.getParent()).removeAllViews();
            });
            builder.show();

        }

        // 确认添加数据
        private void confirmAdd() {
            String r = mRChannelEd.getText().toString().trim();
            String g = mGChannelEd.getText().toString().trim();
            String b = mBChannelEd.getText().toString().trim();
            String potency = mPotencyChannelEd.getText().toString().trim();

            int lenR = r.length();
            int lenG = g.length();
            int lenB = b.length();
            int lenPotency = potency.length();

            if (Local.isChineseLanguage()) {
                //数据校验
                //rgb三者至少要有一个存在
                if (lenR <= 0 && lenG == 0 && lenB == 0) {
                    toast("rbg值至少要有一个存在");
                    return;
                }
                if (lenPotency <= 0) {
                    toast("浓度值不能为空");
                    return;
                }
                //判断rbg通道值是否是在0-255之间
                if (lenR > 0) {
                    int rInt = Integer.parseInt(r);
                    if (rInt < 0 || rInt > 255) {
                        toast("R通道值不在0-255之间");
                        return;
                    }
                }
                if (lenG > 0) {
                    int gInt = Integer.parseInt(g);
                    if (gInt < 0 || gInt > 255) {
                        toast("G通道值不在0-255之间");
                        return;
                    }
                }
                if (lenB > 0) {
                    int bInt = Integer.parseInt(b);
                    if (bInt < 0 || bInt > 255) {
                        toast("B通道值不在0-255之间");
                        return;
                    }
                }
            } else {
                if (lenR <= 0 && lenG == 0 && lenB == 0) {
                    toast("At least one value of RGB must be present");
                    return;
                }
                if (lenPotency <= 0) {
                    toast("Potency value cannot be empty");
                    return;
                }
                //Check if the RGB channel values are between 0-255
                if (lenR > 0) {
                    int rInt = Integer.parseInt(r);
                    if (rInt < 0 || rInt > 255) {
                        toast("Value of R channel is not between 0-255");
                        return;
                    }
                }
                if (lenG > 0) {
                    int gInt = Integer.parseInt(g);
                    if (gInt < 0 || gInt > 255) {
                        toast("Value of G channel is not between 0-255");
                        return;
                    }
                }
                if (lenB > 0) {
                    int bInt = Integer.parseInt(b);
                    if (bInt < 0 || bInt > 255) {
                        toast("Value of B channel is not between 0-255");
                        return;
                    }
                }

            }

            //默认填充数据
            if (lenR == 0) {
                r = "0";
            }
            if (lenG == 0) {
                g = "0";
            }
            if (lenB == 0) {
                b = "0";
            }
            //将rbg转hex（长度标准#+6位）
            String hex = "#" + String.format("%02x", Integer.parseInt(r)) + String.format("%02x", Integer.parseInt(g)) + String.format("%02x", Integer.parseInt(b));
            //添加数据
            //获取模型数据字符串
            String dataStr = mSeqModel.getData();
            if (dataStr == null || dataStr.length() == 0) {
                dataStr = hex + ":" + potency;
            } else {
                dataStr += "," + hex + ":" + potency;
            }
            Log.e(TAG, "confirmAdd: " + "id = " + mSeqModel.getId() + " dataStr = " + dataStr);
            //更新数据库信息
            mModelDao.updateData(mSeqModel.getId(), dataStr);
            toast(Local.isChineseLanguage() ? "添加成功" : "Success");
            clearEditText();
            //更新数据
            loadModel();
            mDataListAdapter.updateDataSet(new ArrayList<String>(mModel.getData().keySet()));
            setViewData();
        }

        private void clearEditText() {
            mRChannelEd.setText("");
            mGChannelEd.setText("");
            mBChannelEd.setText("");
            mPotencyChannelEd.setText("");
        }

    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //右上角菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Local.isChineseLanguage()) {
            //添加 训练并保存、删除模型选项 平铺文字
            menu.add(0, 0, 0, "训练并保存").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, 1, 0, "删除模型").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menu.add(0, 0, 0, "Train and Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, 1, 0, "Delete").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }


}