package com.wen.spark.project.session.test;

import com.wen.spark.project.session.jdbc.JDBCHelper;

import java.sql.Connection;

public class JDBCTest {
    public static void main(String[] args) {
        JDBCHelper jdbcHelper=    JDBCHelper.getInstance();
        Connection connection=jdbcHelper.getConnection();
        jdbcHelper.BackConnection(connection);
    }
}
