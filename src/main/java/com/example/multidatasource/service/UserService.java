package com.example.multidatasource.service;

import com.example.multidatasource.config.DataSourceType;
import com.example.multidatasource.config.UsingDataSource;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUser(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }

    @UsingDataSource
    public User getUserFromDefault(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }

    @UsingDataSource(DataSourceType.SLAVE)
    public User getUserFromSlave1(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }

    @UsingDataSource(DataSourceType.SLAVE_2)
    public User getUserFromSlave2(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }
}
