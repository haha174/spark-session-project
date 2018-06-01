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
    interface SPARK{
        public static final String SPARK_APP_NAME_SESSION="spark-session";
        public static final String SPARK_LOCAL="spark_local";
    }
    interface FIELD{
        public static final String FIELD_AGE="age";
        public static final String FIELD_PROFESSIONAL="professional";
        public static final String FIELD_CITY="city";
        public static final String FIELD_SEX="spark_local";
    }
    interface SESSION_PROJECT{
        public static final String FIELD_SESSION_ID="sessionid";
        public static final String PARAM_START_DATE="startDate";
        public static final String PARAM_END_DATE="endDate";

        public static final String FIELD_CITY="city";
        public static final String FIELD_SEX="spark_local";

        String FIELD_SEARCH_KEYWORDS = "searchKeywords";
        String FIELD_CLICK_CATEGORY_IDS = "clickCategoryIds";
    }

}
