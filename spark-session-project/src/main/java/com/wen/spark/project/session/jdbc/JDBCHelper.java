package com.wen.spark.project.session.jdbc;

import com.wen.spark.project.session.conf.ConfigurationManager;
import com.wen.spark.project.session.conf.Constants;
import com.wen.spark.project.session.util.GetValueUtils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class JDBCHelper {


    // 数据库连接池
    private ConcurrentLinkedQueue<Connection> datasource = new ConcurrentLinkedQueue<Connection>() ;


    static {
        try {
            String driver = ConfigurationManager.getProperty(Constants.JDBC.JDBC_DRIVER);
            maxNum = GetValueUtils.getIntegerOrElse(ConfigurationManager.getProperty(Constants.JDBC.JDBC_DATASOURCE_MAX_SIZE), 20);
            url = ConfigurationManager.getProperty(Constants.JDBC.JDBC_URL);
            user = ConfigurationManager.getProperty(Constants.JDBC.JDBC_USER);
            password = ConfigurationManager.getProperty(Constants.JDBC.JDBC_PASSWORD);
            getConnectionTime = GetValueUtils.getIntegerOrElse(ConfigurationManager.getProperty(Constants.JDBC.JDBC_DATASOURCE_RETEY_TIME), 3);
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 第二步，实现JDBCHelper的单例化
    // 为什么要实现代理化呢？因为它的内部要封装一个简单的内部的数据库连接池
    // 为了保证数据库连接池有且仅有一份，所以就通过单例的方式
    // 保证JDBCHelper只有一个实例，实例中只有一份数据库连接池
    private static JDBCHelper instance = null;

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static JDBCHelper getInstance() {
        if (instance == null) {
            synchronized (JDBCHelper.class) {
                if (instance == null) {
                    instance = new JDBCHelper();
                }
            }
        }
        return instance;
    }


    private JDBCHelper() {
        int datasourceSize = GetValueUtils.getIntegerOrElse(ConfigurationManager.getProperty(Constants.JDBC.JDBC_DATASOURCE_SIZE), 1);
        try {
            initDataSourcre(datasourceSize);
        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    private synchronized void initDataSourcre(int datasourceSize) throws Exception {
        for (int i = 0; i < datasourceSize; i++) {
            Connection conn = DriverManager.getConnection(url, user, password);
            datasource.add(conn);
            addNum(1);
        }
    }

    /**
     * 第四步，提供获取数据库连接的方法
     * 有可能，你去获取的时候，这个时候，连接都被用光了，你暂时获取不到数据库连接
     * 所以我们要自己编码实现一个简单的等待机制，去等待获取到数据库连接
     */
    public synchronized Connection getConnection() {
        try {
            int time = 0;
            while (datasource.size() == 0) {
                time++;
                if (time > getConnectionTime && num <= maxNum) {
                    initDataSourcre(1);
                } else {
                    Thread.sleep(100);
                }
            }
            Connection connection = datasource.poll();
            if (connection.isClosed()) {
                reduceNum(1);
                return getConnection();
            }
            connection.setAutoCommit(true);
            return connection;
        } catch (Exception e) {
            reduceNum(1);
            return getConnection();
        }
    }

    public synchronized void BackConnection(Connection connection) {
        datasource.add(connection);
    }

    /**
     * 执行增删改SQL语句
     *
     * @param sql
     * @return 影响的行数
     */
    public int executeUpdate(PreparedStatement pstmt) throws SQLException{
        int rtn=0;
        rtn = pstmt.executeUpdate();
        return rtn;
    }

    /**
     * 执行查询SQL语句
     *
     * @param sql
     * @param params
     */
    public ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    /**
     *
     * @param connection
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    public PreparedStatement getPrepareStatementSql(Connection connection,String sql, Object[] params)throws SQLException{
        PreparedStatement  pstmt = connection.prepareStatement(sql);
        if(params!=null&&params.length>0){
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
        return pstmt;
    }

    /**
     * 不需要处理事务
      * @param sql
     * @param params
     * @return
     * @throws SQLException
     */

    public PreparedStatement getPrepareStatementSql(String sql, Object[] params)throws SQLException{
        Connection connection=getConnection();
        PreparedStatement  pstmt=null;
       try {
            pstmt = connection.prepareStatement(sql);
            if(params!=null&&params.length>0){
                for (int i = 0; i < params.length; i++) {
                   pstmt.setObject(i + 1, params[i]);
               }
           }

       }catch (Exception e){
            e.printStackTrace();
       }finally {
           BackConnection(connection);
       }
        return pstmt;
    }

    /**
     * 批量执行SQL语句
     * <p>
     * 批量执行SQL语句，是JDBC中的一个高级功能
     * 默认情况下，每次执行一条SQL语句，就会通过网络连接，向MySQL发送一次请求
     * <p>
     * 但是，如果在短时间内要执行多条结构完全一模一样的SQL，只是参数不同
     * 虽然使用PreparedStatement这种方式，可以只编译一次SQL，提高性能，但是，还是对于每次SQL
     * 都要向MySQL发送一次网络请求
     * <p>
     * 可以通过批量执行SQL语句的功能优化这个性能
     * 一次性通过PreparedStatement发送多条SQL语句，比如100条、1000条，甚至上万条
     * 执行的时候，也仅仅编译一次就可以
     * 这种批量执行SQL语句的方式，可以大大提升性能
     *
     * @param sql
     * @param paramsList
     * @return 每条SQL语句影响的行数
     */

    public int[] executeBatch( Connection conn,String sql, List<Object[]> paramsList) throws SQLException{
        int[] rtn = null;
        PreparedStatement pstmt = null;
        // 第一步：使用Connection对象，取消自动提交
        pstmt = conn.prepareStatement(sql);
        // 第二步：使用PreparedStatement.addBatch()方法加入批量的SQL参数
        for (Object[] params : paramsList) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.addBatch();
        }
        // 第三步：使用PreparedStatement.executeBatch()方法，执行批量的SQL语句
        rtn = pstmt.executeBatch();
        // 最后一步：使用Connection对象，提交批量的SQL语句
        return rtn;
    }

    private int num;

    private synchronized void addNum(int num) {
        this.num += num;
    }

    private synchronized void reduceNum(int num) {
        this.num -= num;
    }

    private static int maxNum = 20;
    private static String url;
    private static String user;
    private static String password;
    private static int getConnectionTime;

}
