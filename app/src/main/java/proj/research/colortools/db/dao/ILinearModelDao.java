package proj.research.colortools.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import proj.research.colortools.db.LocalDatabase;
import proj.research.colortools.db.model.LinearSeqModel;

@Dao
public interface ILinearModelDao {

    @Insert
    void insert(LinearSeqModel model);

    @Query("delete from " + LocalDatabase.TABLE_LINEAR_MODEL + " where id = :id")
    void delete(int id);

    @Update
    void update(LinearSeqModel model);

    /**
     * 更新训练后的信息
     * @param id 被更新数据的id
     * @param weights
     * @param bias
     * @param accuracy
     */
    @Query("update " + LocalDatabase.TABLE_LINEAR_MODEL + " set weights = :weights, bias = :bias, accuracy = :accuracy where id = :id")
    void updateTrainedInfo(int id, String weights, double bias, double accuracy);

    @Query("update " + LocalDatabase.TABLE_LINEAR_MODEL + " set name = :name, ratio = :ratio, error = :error, useR = :useR, useG = :useG, useR = :useB  where id = :id")
    void updateBaseInfo(int id, String name, double ratio, double error, boolean useR, boolean useG, boolean useB);


    /**
     * 根据id更新数据集字符串
     * @param id id
     * @param data 新的字符串
     */
    @Query("update " + LocalDatabase.TABLE_LINEAR_MODEL + " set data = :data where id = :id")
    void updateData(int id, String data);

    @Query("select * from " + LocalDatabase.TABLE_LINEAR_MODEL + " where id = :id")
    LinearSeqModel query(int id);

    @Query("select * from " + LocalDatabase.TABLE_LINEAR_MODEL)
    List<LinearSeqModel> queryAll();

    //根据名称查询数量
    @Query("select count(*) from " + LocalDatabase.TABLE_LINEAR_MODEL + " where name = :name")
    int queryCountByName(String name);

}
