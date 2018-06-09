package com.wen.spark.project.session.dao;


import com.wen.spark.project.session.bean.SessionFactory;
import com.wen.spark.project.session.domain.SessionRandomExtract;
import com.wen.spark.project.session.jdbc.JDBCHelper;

/**
 * 随机抽取session的DAO实现
 * @author wchen129
 *
 */
public class SessionRandomExtractDAOImpl implements ISessionRandomExtractDAO {

	/**
	 * 插入session随机抽取
	 * @param sessionRandomExtract
	 */
	@Override
	public void insert(SessionRandomExtract sessionRandomExtract) {
		String sql = "insert into session_random_extract values(?,?,?,?,?)";
		
		Object[] params = new Object[]{sessionRandomExtract.getTaskid(),
				sessionRandomExtract.getSessionid(),
				sessionRandomExtract.getStartTime(),
				sessionRandomExtract.getSearchKeywords(),
				sessionRandomExtract.getClickCategoryIds()};
        SessionFactory sessionFactory=SessionFactory.getSessionFactory ();
        sessionFactory.executeUpdate(sql, params);
	}
	
}
