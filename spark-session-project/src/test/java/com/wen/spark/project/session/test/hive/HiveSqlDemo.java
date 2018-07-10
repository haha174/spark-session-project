package com.wen.spark.project.session.test.hive;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author : WChen129
 * @date : 2018-06-21
 */
public class HiveSqlDemo {
    public static void main(String[] args) {
        SparkConf conf=new SparkConf().setAppName("HiveDataSource").setMaster("local");
        JavaSparkContext sc=new JavaSparkContext(conf);

        Configuration configuration=sc.hadoopConfiguration();

        configuration.set("dfs.client.use.datanode.hostname","true");

        SparkSession  sqlContext = SparkSession.builder().enableHiveSupport().config(conf).getOrCreate();

        // 创建HiveContext  注意这里接收的是SparkContext   不是 JavaSparkContext
        //  HiveContext sqlContext=new HiveContext(sc.sc());

        //sqlContext.setConf("hive.metastore.uris","thrift://cloud.codeguoj.cn:9083");

        //ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE

        //第一个功能，使用HiveContext的Sql()/Hql
        sqlContext.sql("DROP TABLE IF EXISTS student_info");

        sqlContext.sql("CREATE  TABLE IF NOT EXISTS student_info (name STRING ,age INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE");

        System.out.println("============================create table success");
        //将学生的基本信息导入到StudentInfo  表
        sqlContext.sql("LOAD DATA LOCAL INPATH '/data/hive/student_info/student_info.txt' INTO TABLE  student_info");

        sqlContext.sql("DROP TABLE IF EXISTS student_scores");

        sqlContext.sql("CREATE  TABLE IF NOT EXISTS student_scores (name STRING ,score INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE");
        //将学生的基本分数导入到StudentInfo  表
        sqlContext.sql("LOAD DATA LOCAL INPATH '/data/hive/student_info/student_scores.txt' INTO TABLE  student_scores ");
        //第二个功能接着将sql  返回的DataFrame  用于查询
        //执行sql  关联两张表查询大于80分的学生
        Dataset studentInfo=  sqlContext.sql("SELECT * FROM student_info");
        studentInfo.show();

        Dataset studentScore=  sqlContext.sql("SELECT * FROM student_scores");
        studentScore.show();

        Dataset goodStudentDS=sqlContext.sql("SELECT ss.name ,s1.age,ss.score from student_info s1 JOIN  student_scores ss ON s1.name=ss.name WHERE   ss.score>=80");

        goodStudentDS.show();

        //第三个功能，可以将 DataFrame  中的数据 理论上来说DataFrame  对应的RDD  数据  是ROW  即可
        //将DataFrame  保存到Hive  表中·
        //  接着将数据保存到good_student_info  中

        sqlContext.sql("DROP TABLE IF EXISTS good_student_info");


        //     System.out.println("create table success");
        goodStudentDS.write().saveAsTable("good_student_info");
        //  第四个功能 针对  good_student_info  表  直接创建   DataSet
        Dataset<Row> goodStudentDSRows=sqlContext.sql("SELECT * FROM  good_student_info");
        goodStudentDSRows.show();
//        Row[] goodStudentRows=goodStudentDSRows.collect();
//        for (Row goodStudentRow:goodStudentRows){
//            System.out.println(goodStudentRow);
//        }
        sc.close();
    }
}
