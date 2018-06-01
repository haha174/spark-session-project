package com.wen.spark.project.session.dao;

import com.wen.spark.project.session.bean.SessionFactory;
import com.wen.spark.project.session.entrty.Task;

/**
 * @author WChen129
 * @date 2018-05-24
 */
public class TaskDAOImpl implements ITaskDAO {

	/**
	 * 根据主键查询任务
	 * @param taskid 主键
	 * @return 任务
	 */
	@Override
    public Task findById(long taskid) {
		String sql = "select * from task where task_id=?";
		Object[] params = new Object[]{taskid};
        SessionFactory sessionFactory=SessionFactory.getSessionFactory();
        return   sessionFactory.queryForObject(sql,params,Task.class);
	}
	
}
