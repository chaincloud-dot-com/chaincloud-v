package com.chaincloud.chaincloudv.dao;

import com.chaincloud.chaincloudv.model.AddressBatch;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by zhumingu on 16/6/16.
 */
public class AddressBatchDao extends RuntimeExceptionDao<AddressBatch, Integer> {

    private Dao<AddressBatch, Integer> dao;

    public AddressBatchDao(Dao<AddressBatch, Integer> dao) {
        super(dao);

        this.dao = dao;
    }

    public List<AddressBatch> getByType(AddressBatch.Type type) {
        try {
            return dao.queryBuilder()
                    .orderBy("index", true)
                    .where().eq("type", type)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public AddressBatch getByIndex(int index, AddressBatch.Type type){
        try {
            return dao.queryBuilder()
                    .where().eq("index", index)
                        .and().eq("type", type)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
