package org.ilri.eweigh.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import org.ilri.eweigh.cattle.models.Breed;
import org.ilri.eweigh.cattle.models.Cattle;
import org.ilri.eweigh.cattle.models.Dosage;
import org.ilri.eweigh.database.dao.BreedsDao;
import org.ilri.eweigh.database.dao.CattleDao;
import org.ilri.eweigh.database.dao.DosagesDao;
import org.ilri.eweigh.database.dao.FeedsDao;
import org.ilri.eweigh.database.dao.SubmissionsDao;
import org.ilri.eweigh.feeds.Feed;
import org.ilri.eweigh.hg_lw.models.Submission;

@Database(entities = {
        Breed.class,
        Cattle.class,
        Dosage.class,
        Feed.class,
        Submission.class
}, version = 7, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract BreedsDao breedsDao();
    public abstract CattleDao cattleDao();
    public abstract DosagesDao dosagesDao();
    public abstract FeedsDao feedsDao();
    public abstract SubmissionsDao submissionsDao();

    public static AppDatabase getAppDatabase(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                    "ewdb")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance(){
        INSTANCE = null;
    }

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }
}
