package com.chaincloud.chaincloudv.dao;

import com.chaincloud.chaincloudv.model.Channel;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;

/**
 * Created by zhumingu on 16/6/16.
 */
public class ChannelDao extends RuntimeExceptionDao<Channel, Integer> {
    private Dao<Channel, Integer> dao;

    public ChannelDao(Dao<Channel, Integer> dao) {
        super(dao);

        this.dao = dao;
    }

    public Channel getLastChannel(){
        try {
            return dao.queryBuilder()
                    .orderBy("channel_id", false)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Channel getOkChannel() {
        try {
            return dao.queryBuilder()
                    .orderBy("channel_id", false)
                    .where().eq("channel_status", Channel.ChannelStatus.OK)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Channel getByCId(int cId) {
        try {
            return dao.queryBuilder()
                    .where().eq("c_id", cId)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
