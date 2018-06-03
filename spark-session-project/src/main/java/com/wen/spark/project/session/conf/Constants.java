package com.wen.spark.project.session.conf;

public interface Constants {
     interface JDBC{
         String  JDBC_DRIVER="jdbc.driver";
         String  JDBC_URL="jdbc.url";
         String  JDBC_USER="jdbc.username";
         String  JDBC_PASSWORD="jdbc.password";
         String  JDBC_DATASOURCE_SIZE="jdbc.datasource.size";
         String  JDBC_DATASOURCE_MAX_SIZE="jdbc.datasource.max.size";
         String  JDBC_DATASOURCE_RETEY_TIME="jdbc.datasource.retry.time";

    }
    interface PATTERN{
         String  PATTERN_CHECK_NUMBER="^\\s*[0-9]+\\s*$";
    }
    interface SPARK{
         String SPARK_APP_NAME_SESSION="spark-session";
         String SPARK_LOCAL="spark_local";
    }
    interface FIELD{
         String FIELD_AGE="age";
         String FIELD_PROFESSIONAL="professional";
         String FIELD_CITY="city";
         String FIELD_SEX="spark_local";
    }
    interface SESSION_PROJECT{
        String FIELD_SEX="spark_local";

        String FIELD_SESSION_ID="sessionid";
         String PARAM_START_DATE="startDate";
         String PARAM_END_DATE="endDate";
         String FIELD_CITY="city";
        String FIELD_SEARCH_KEYWORDS = "searchKeywords";
        String FIELD_CLICK_CATEGORY_IDS = "clickCategoryIds";
        String PARAM_SEX="sex";
        String PARAM_KEYWORDS="keywords";
        String PARAM_CITIES="cities";
        String PARAM_PROFESSIONALS="professionals";
        String PARAM_ENDAGE="endAge";
        String PARAM_STARTAGE="startAge";
        String PARAM_CATEGORY_IDS="categoryIds";

        String SESSION_COUNT = "session_count";
        String TIME_PERIOD_1s_3s = "1s_3s";
        String TIME_PERIOD_4s_6s = "4s_6s";
        String TIME_PERIOD_7s_9s = "7s_9s";
        String TIME_PERIOD_10s_30s = "10s_30s";
        String TIME_PERIOD_30s_60s = "30s_60s";
        String TIME_PERIOD_1m_3m = "1m_3m";
        String TIME_PERIOD_3m_10m = "3m_10m";
        String TIME_PERIOD_10m_30m = "10m_30m";
        String TIME_PERIOD_30m = "30m";
        String STEP_PERIOD_1_3 = "1_3";
        String STEP_PERIOD_4_6 = "4_6";
        String STEP_PERIOD_7_9 = "7_9";
        String STEP_PERIOD_10_30 = "10_30";
        String STEP_PERIOD_30_60 = "30_60";
        String STEP_PERIOD_60 = "60";

    }


}
