package com.wen.spark.project.session.test;

import com.wen.spark.project.session.bean.SessionFactory;
import com.wen.spark.project.session.entrty.UserEntrty;
import com.wen.spark.project.session.jdbc.JDBCHelper;

import java.sql.Connection;

public class JDBCTest {
    public static void main(String[] args) {
        SessionFactory sessionFactory=SessionFactory.getSessionFactory();
//      UserEntrty userEntrty= sessionFactory.queryForObject("select * from user",UserEntrty.class);
        System.out.println();
        sessionFactory.queryForMap("select table_name from test.tables");
    }
}
