package proj.research.colortools.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import proj.research.colortools.db.dao.ILinearModelDao;
import proj.research.colortools.db.model.LinearSeqModel;

@Database(entities = {LinearSeqModel.class}, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {
    public static final String TABLE_LINEAR_MODEL = "linear_model";

    private static LocalDatabase mInstance;
    private static final String DB_NAME = "local.db";


    public static synchronized LocalDatabase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = Room.databaseBuilder(context, LocalDatabase.class, DB_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return mInstance;
    }

    public abstract ILinearModelDao getLinearModelDao();

}