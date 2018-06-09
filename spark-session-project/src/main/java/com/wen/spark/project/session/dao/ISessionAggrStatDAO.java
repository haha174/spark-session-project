package com.wen.spark.project.session.dao;

import com.wen.spark.project.session.domain.SessionAggrStat;

/**
 * session聚合统计模块DAO接口
 * @author wchen129
 *
 */
public interface ISessionAggrStatDAO {
	
	void insert (SessionAggrStat sessionAggrStat);

}