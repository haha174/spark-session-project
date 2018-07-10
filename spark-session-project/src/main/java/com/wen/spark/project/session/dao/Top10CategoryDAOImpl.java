package com.wen.spark.project.session.dao;

import com.wen.spark.project.session.bean.SessionFactory;
import com.wen.spark.project.session.domain.Top10Category;

public class Top10CategoryDAOImpl implements ITop10CategoryDAO{
    @Override
	public void insert(Top10Category category) {
		String sql = "insert into top10_category values(?,?,?,?,?,?)";
		Object[] params = new Object[]{
				category.getTaskid(),
				category.getCategoryid(),
				category.getClickCount(),
				category.getOrderCount(),
				category.getPayCount()
		        ,category.getUUID ()
		};

        SessionFactory sessionFactory=SessionFactory.getSessionFactory ();
        sessionFactory.executeUpdate ( sql,params );
	}

}