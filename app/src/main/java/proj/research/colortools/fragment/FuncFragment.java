package proj.research.colortools.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import proj.research.colortools.ModelDetailActivity;
import proj.research.colortools.R;
import proj.research.colortools.db.LocalDatabase;
import proj.research.colortools.db.dao.ILinearModelDao;
import proj.research.colortools.db.model.LinearSeqModel;
import proj.research.colortools.util.Local;

public class FuncFragment extends Fragment {
    private static final String TAG = "FuncFragment";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 66;
    private Context mContext;
    private View mRootView;
    private RecyclerView mFuncRv;
    private FloatingActionButton mAddFab;
    private List<LinearSeqModel> mModelList;
    private ILinearModelDao mModelDao;
    private LocalDatabase mDB;
    private boolean mIsChinese;
    private ModelAdapter mListAdapter;

    public FuncFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.fragment_func, container, false);
        initView();
        initData();
        initAdapterAndListener();
        return mRootView;
    }

    private void initData() {
        mIsChinese = Local.isChineseLanguage();
        //获取数据库中的模型信息
        mDB = LocalDatabase.getInstance(mContext);
        mModelDao = mDB.getLinearModelDao();
        loadModelList();
        Log.e(TAG, "modellist" + mModelList.size());
    }

    private void loadModelList() {
        mModelList = mModelDao.queryAll();
        if(mListAdapter != null){
            mListAdapter.notifyDataSetChanged();
        }

    }


    /**
     * 初始化视图
     */
    private void initView() {
        mFuncRv = mRootView.findViewById(R.id.func_list);
        mAddFab = mRootView.findViewById(R.id.func_add);
    }

    private void initAdapterAndListener() {
        //设置竖直方向的线性布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //设置适配器
        mFuncRv.setLayoutManager(layoutManager);
        mFuncRv.addItemDecoration(new FuncItemDecoration());
        mListAdapter = new ModelAdapter();
        mFuncRv.setAdapter(mListAdapter);

        mAddFab.setOnClickListener(v -> {
            addModel();
        });
    }

    /**
     * 增加模型
     */
    private void addModel() {
        //检查权限
        // 检查是否拥有读取外部存储器的权限
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有权限，则请求权限
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mIsChinese ? "新建模型": "New Model");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        EditText editText = new EditText(mContext);
        editText.setPadding(20, 20, 20, 20);
        editText.setLayoutParams(params);
        editText.setHint(mIsChinese ? "请输入模型名称(2-10个字符)": "Please input model name(2-10 characters)");

        builder.setView(editText);
        builder.setPositiveButton(mIsChinese ? "确定" : "Confirm", (dialog, which) -> {
            String modelName = editText.getText().toString().trim();
            if (modelName.isEmpty() || modelName.length() < 2 || modelName.length() > 7) {
                String msg = mIsChinese ? "模型名称不正确" : "Model name is not correct";
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            } else {
                LinearSeqModel lsm = new LinearSeqModel();
                lsm.setName(modelName);
                mModelDao.insert(lsm);
                loadModelList();
            }
        });
        builder.setNegativeButton(mIsChinese ? "取消" : "Cancel", null);
        //导入模型按钮
        builder.setNeutralButton(mIsChinese ? "导入模型" : "Import", (dialog, which) -> {
            //跳转到文件选择页面，选择 .model 后缀的文件
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);

        });
        builder.show();
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //接收选择的文件并导入模型
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){

            try {
                Uri uri = data.getData();
                String path = uri.getPath();
                Log.e(TAG, "onActivityResult: " + path);
                //将文件复制到本地
                File file = new File(path);
                String fileName = file.getName();
                String suffix = fileName.substring(fileName.lastIndexOf("."));
                if(suffix.equals(".model")){
                    //复制文件到本地
                    LinearSeqModel imported = Local.importModel(mContext, mContext.getContentResolver().openInputStream(uri));
                    if(imported == null){
                        String tip =  mIsChinese ? "导入模型失败" : "Import model failed";
                        Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
                    }else{
                        String tip =  mIsChinese ? "导入模型成功" : "Import model success";
                        Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
                        loadModelList();
                    }
                }else{
                    Toast.makeText(mContext, "文件格式不正确", Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onActivityResult: " + e.getMessage());
                Toast.makeText(mContext, "导入模型失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //重新加载模型
        loadModelList();
        Log.e(TAG, "onResume: " + mModelList.size());
    }

    private class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.InnerHolder> {

        @NonNull
        @Override
        public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            mRootView = LayoutInflater.from(mContext).inflate(R.layout.item_model, parent, false);
            return new InnerHolder(mRootView);
        }

        @Override
        public void onBindViewHolder(@NonNull InnerHolder holder, int position) {
            //填充数据
            holder.setViewData(mModelList.get(position));
            //设置点击事件
            holder.itemView.setOnClickListener(v -> {
                //将模型id传递到模型详情页面并跳转
                Bundle bundle = new Bundle();
                bundle.putInt("model_id", mModelList.get(position).getId());
                startActivity(new Intent(mContext, ModelDetailActivity.class).putExtras(bundle));
            });
            //设置删除事件
            holder.mDeleteIv.setOnClickListener(v -> {
                deleteModel(position);
            });
            //设置长按事件
            holder.itemView.setOnLongClickListener(v -> {
                outputModel(position);
                return true;
            });

        }

        /**
         * 导出模型
         * @param position
         */
        private void outputModel(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            if(mIsChinese) {
                builder.setTitle("导出模型");
                builder.setMessage("确定导出模型[" + mModelList.get(position).getName() + "]吗？");
            }else{
                builder.setTitle("Export");
                builder.setMessage("Are you sure to export model [" + mModelList.get(position).getName() + "]?");
            }
            builder.setPositiveButton(mIsChinese ? "确定" : "Confirm", (dialog, which) -> {
                exportModel(mModelList.get(position));
            });
            builder.setNegativeButton(mIsChinese ? "取消" : "Cancel", null);
            builder.show();
        }

        /**
         * 导出模型
         * @param model 模型信息
         */
        private void exportModel(LinearSeqModel model) {
            try {
                //获取模型信息
                boolean success = Local.exportModel(mContext, model);
                if(!success){
                    Toast.makeText(mContext, "导出模型失败", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "exportModel: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void deleteModel(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            if(mIsChinese) {
                builder.setTitle("删除模型");
                builder.setMessage("确定删除模型[" + mModelList.get(position).getName() + "]吗？");
            }else{
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete model [" + mModelList.get(position).getName() + "]?");
            }
            builder.setPositiveButton(mIsChinese ? "确定" : "Confirm", (dialog, which) -> {
                mModelDao.delete(mModelList.get(position).getId());
                loadModelList();
                notifyDataSetChanged();
            });
            builder.setNegativeButton(mIsChinese ? "取消" : "Cancel", null);
            builder.show();
        }

        @Override
        public int getItemCount() {
            return mModelList.size();
        }

        public class InnerHolder extends RecyclerView.ViewHolder {
            private View mItemView;
            public TextView mModelNameTv;
            public TextView mAccuracyTv;
            public ImageView mDeleteIv;

            public InnerHolder(@NonNull View itemView) {
                super(itemView);
                mItemView = itemView;
                mModelNameTv = itemView.findViewById(R.id.model_name);
                mAccuracyTv = itemView.findViewById(R.id.model_accuracy);
                mDeleteIv = itemView.findViewById(R.id.model_delete);
            }

            public void setViewData(LinearSeqModel model) {
                mModelNameTv.setText(model.getName());
                double accuracy = model.getAccuracy();
                //保留两位小数并转化为百分数
                if(mIsChinese) {
                    mAccuracyTv.setText("准确率: " + String.format("%.2f", accuracy * 100) + "%");
                }else{
                    mAccuracyTv.setText("Accuracy: " + String.format("%.2f", accuracy * 100) + "%");
                }
            }

        }
    }

    private class FuncItemDecoration extends RecyclerView.ItemDecoration {
        /**
         * @param outRect 边界
         * @param view    recyclerView ItemView
         * @param parent  recyclerView
         * @param state   recycler 内部数据管理
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //设定底部边距为1px
            outRect.set(0, 0, 0, 2);
        }
    }
}