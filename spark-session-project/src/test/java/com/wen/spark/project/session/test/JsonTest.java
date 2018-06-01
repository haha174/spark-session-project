package com.wen.spark.project.session.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * @author : WChen129
 * @date : 2018-05-25
 */
public class JsonTest {
    public static void main(String[] args) {
        String json = "[{'学生':'张三', '班级':'一班', '年级':'大一', '科目':'高数', '成绩':90}, {'学生':'李四', '班级':'二班', '年级':'大一', '科目':'高数', '成绩':80}]";

        JSONArray jsonArray = JSONArray.parseArray(json);
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        System.out.println(jsonObject.getString("学生"));
    }
}
