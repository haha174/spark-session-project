package com.wen.spark.project.session.dao;

import com.wen.spark.project.session.entrty.Task;

/**
 * @author WChen129
 * @date 2018-05-24
 */
public interface ITaskDAO {
	
	/**
	 * 根据主键查询任务
	 * @param taskid 主键
	 * @return 任务
	 */
	Task findById(long taskid);
	
}
