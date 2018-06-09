package com.wen.spark.project.session.factory;

import com.wen.spark.project.session.dao.ISessionAggrStatDAO;
import com.wen.spark.project.session.dao.ITaskDAO;
import com.wen.spark.project.session.dao.SessionAggrStatDAOImpl;
import com.wen.spark.project.session.dao.TaskDAOImpl;

/**
 * @author WChen129
 * @date 2018-05-24
 */
public class DAOFactory {

	/**
	 * 获取任务管理DAO
	 * @return DAO
	 */
	public static ITaskDAO getTaskDAO() {
		return new TaskDAOImpl();
	}
    public static ISessionAggrStatDAO getSessionAggrStatDAO() {
        return new SessionAggrStatDAOImpl ();
    }

}