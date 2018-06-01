package com.wen.spark.project.session.Exception;

public class JDBCException extends RuntimeException {
    public JDBCException(String ression){
        super(ression);
    }

    public JDBCException(String ression ,Throwable cause){
        super(ression,cause);
    }
}
