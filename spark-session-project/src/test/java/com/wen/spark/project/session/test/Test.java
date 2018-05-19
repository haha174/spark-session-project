package com.wen.spark.project.session.test;

import com.wen.spark.project.session.Exception.SessionFactoryException;
import com.wen.spark.project.session.bean.SessionFactory;
import com.wen.spark.project.session.entrty.UserEntrty;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        SessionFactory sessionFactory=SessionFactory.getSessionFactory();
        UserEntrty userEntrty= sessionFactory.queryForObject("select * from user",UserEntrty.class);
        System.out.println();
    }
}


