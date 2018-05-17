package com.wen.spark.project.session.conf;

public interface Constants {
     interface JDBC{
        public static final String  JDBC_DRIVER="jdbc.driver";
        public static final String  JDBC_URL="jdbc.url";
        public static final String  JDBC_USER="jdbc.username";
        public static final String  JDBC_PASSWORD="jdbc.password";
        public static final String  JDBC_DATASOURCE_SIZE="jdbc.datasource.size";
        public static final String  JDBC_DATASOURCE_MAX_SIZE="jdbc.datasource.max.size";
        public static final String  JDBC_DATASOURCE_RETEY_TIME="jdbc.datasource.retry.time";



    }
    interface PATTERN{
        public static final String  PATTERN_CHECK_NUMBER="^\\s*[0-9]+\\s*$";
    }
}
