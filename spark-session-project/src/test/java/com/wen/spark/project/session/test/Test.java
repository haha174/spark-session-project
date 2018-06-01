package com.wen.spark.project.session.test;

import com.wen.spark.project.session.Exception.SessionFactoryException;
import com.wen.spark.project.session.bean.SessionFactory;
import com.wen.spark.project.session.dao.ITaskDAO;
import com.wen.spark.project.session.dao.TaskDAOImpl;
import com.wen.spark.project.session.entrty.Task;
import com.wen.spark.project.session.factory.DAOFactory;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {

        ITaskDAO taskDAO= DAOFactory.getTaskDAO();
        Task task=taskDAO.findById(10000001L);
        if(task!=null){
            System.out.println(task.getTaskName());
        }
    }
}


