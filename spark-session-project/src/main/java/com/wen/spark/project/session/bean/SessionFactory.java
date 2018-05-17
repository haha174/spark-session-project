package com.wen.spark.project.session.bean;

import com.google.common.collect.Lists;
import com.wen.spark.project.session.Exception.SessionFactoryException;
import com.wen.spark.project.session.jdbc.JDBCHelper;
import com.wen.spark.project.session.util.BeanUtil;

import javax.management.Query;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionFactory {
    private Connection connection;
    /**
     * 获得对象
     */
    private JDBCHelper jdbcHelper = JDBCHelper.getInstance();

    public SessionFactory() {
        this.connection = jdbcHelper.getConnection();
    }

    /**
     * 提交事务
     *
     * @return
     */
    public boolean commit() {
        try {
            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 回滚事务
     *
     * @return
     */
    public boolean rollback() {
        try {
            connection.rollback();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置自动提交  默认的是true
     *
     * @return
     */
    public boolean rollback(boolean flag) {
        try {
            connection.setAutoCommit(flag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param sql
     * @param params
     * @return
     */
    public Map<String, Object> queryForMap(String sql, Object[] params) {
        try {
            ResultSet set = jdbcHelper.executeQuery(jdbcHelper.getPrepareStatementSql(connection, sql, params));
            List<Map<String, Object>> list = ResultSetToMap(set);
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SessionFactoryException(e.getMessage());
        }
        return null;
    }

    /**
     * @param sql
     * @return
     */
    public Map<String, Object> queryForMap(String sql) {
        return queryForMap(sql, null);
    }


    private List<Map<String, Object>> ResultSetToMap(ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            String[] name = new String[count];
            for (int i = 0; i < count; i++) {
                name[i] = rsmd.getColumnName(i + 1);
            }
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            Map<String, Object> map = new HashMap<>();
            while (rs.next()) {
                for (int i = 0; i < count; i++) {
                    map.put(name[i], rs.getObject(name[i]));
                }
                result.add(map);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SessionFactoryException(e.getMessage());
        }
    }


    public <T> T queryForObject(String sql, Class<T> clazz) {
        return queryForObject(sql, null, clazz);
    }


    public <T> T queryForObject(String sql, Object[] params, Class<T> clazz) {
        Map<String, Object> map = queryForMap(sql, params);
        if (null != map) {
            try {
                return BeanUtil.mapToBean(clazz,map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public <T> List<T> queryForList(String sql, Class<T> clazz) {
        return queryForList(sql, null, clazz);
    }

    public <T> List<T> queryForList(String sql, Object[] params, Class<T> clazz) {
        try {
            ResultSet set = jdbcHelper.executeQuery(jdbcHelper.getPrepareStatementSql(connection, sql, params));
            List<Map<String, Object>> list = ResultSetToMap(set);
            return mapsToObjects(list, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SessionFactoryException(e.getMessage());
        }
    }

    /**
     * 根据sql  获取String
     *
     * @param sql
     * @return
     */
    public String queryForString(String sql) {
        return queryForString(sql, null);
    }

    /**
     * @param sql
     * @param params
     * @return
     */
    public String queryForString(String sql, Object[] params) {
        try {
            ResultSet set = jdbcHelper.executeQuery(jdbcHelper.getPrepareStatementSql(connection, sql, params));
            List<Map<String, Object>> list = ResultSetToMap(set);
            if (list != null && list.size() > 0) {
                return com.alibaba.fastjson.JSON.toJSONString(list.get(0));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SessionFactoryException(e.getMessage());
        }
        return "";
    }


    private <T> List<T> mapsToObjects(List<Map<String, Object>> maps, Class<T> clazz) throws Exception {
        List<T> list = Lists.newArrayList();
        if (maps != null && maps.size() > 0) {
            T bean = null;
            for (Map<String, Object> map : maps) {
                bean = BeanUtil.mapToBean(clazz,map);
                list.add(bean);
            }
        }
        return list;
    }

    /**
     * 关闭连接
     */
    public void close() {
        JDBCHelper.getInstance().BackConnection(connection);
        this.connection=null;
    }

}
