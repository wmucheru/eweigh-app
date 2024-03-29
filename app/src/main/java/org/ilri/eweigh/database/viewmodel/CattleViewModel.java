package org.ilri.eweigh.database.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ilri.eweigh.cattle.models.Cattle;
import org.ilri.eweigh.database.AppDatabase;
import org.ilri.eweigh.database.dao.CattleDao;

import java.util.List;

public class CattleViewModel extends AndroidViewModel {
    private CattleDao cattleDao;
    private LiveData<List<Cattle>> cattleLiveData;
    private int count;

    public CattleViewModel(@NonNull Application application) {
        super(application);

        cattleDao = AppDatabase.getAppDatabase(application).cattleDao();
        cattleLiveData = cattleDao.get();
        count = cattleDao.getCount();
    }

    public LiveData<List<Cattle>> getAll(){
        return cattleLiveData;
    }

    public int getCount(){
        return count;
    }

    public Cattle getCattle(int id){
        return cattleDao.getCattle(id);
    }

    public void insert(Cattle cattle){
        cattleDao.insert(cattle);
    }

    public void update(Cattle cattle){
        cattleDao.update(cattle);
    }

    public void delete(Cattle cattle){
        cattleDao.delete(cattle);
    }

    public void deleteAll(){
        cattleDao.delete();
    }
}
