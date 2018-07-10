package com.wen.spark.project.session.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class HiveDemo {

    public static void main(String[] args) throws Exception {
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        Connection connection = DriverManager.getConnection("jdbc:hive2://cloud.codeguoj.cn:10000/default","hive","hivedata");
        Statement stmt = connection.createStatement();
        String querySQL="select * from default.student_scores";
        ResultSet resut = stmt.executeQuery(querySQL);
        while (resut.next()) {
            System.out.println(resut.getObject (1));
        }
    }


}