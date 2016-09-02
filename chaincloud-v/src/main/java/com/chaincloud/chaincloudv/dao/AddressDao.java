package com.chaincloud.chaincloudv.dao;

import com.chaincloud.chaincloudv.model.Address;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by zhumingu on 16/6/16.
 */
public class AddressDao extends RuntimeExceptionDao<Address, Integer> {

    private Dao<Address, Integer> dao;

    public AddressDao(Dao<Address, Integer> dao) {
        super(dao);

        this.dao = dao;
    }

    public void addBatch(final List<Address> addresses) {
        try {
            dao.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (Address address : addresses) {
                        dao.create(address);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Address> getByBatchId(Integer batchId) {
        try {
            return dao.queryBuilder()
                    .orderBy("index", true)
                    .where().eq("address_batch", batchId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deleteByBatchId(Integer batchId) {
        try {
            DeleteBuilder<Address, Integer> builder = dao.deleteBuilder();
            builder.where().eq("address_batch", batchId);

            builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
