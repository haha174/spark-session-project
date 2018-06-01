package com.wen.spark.project.session.Exception;

/**
 * @author : WChen129
 * @date : 2018-05-30
 */
public class SessionFactoryException extends RuntimeException {
    public SessionFactoryException(String ression){
        super(ression);
    }

    public SessionFactoryException(String ression ,Throwable cause){
        super(ression,cause);
    }
}
