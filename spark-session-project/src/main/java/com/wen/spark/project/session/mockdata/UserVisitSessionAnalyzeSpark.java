package com.wen.spark.project.session.mockdata;

import com.alibaba.fastjson.JSON;
import com.wen.spark.project.session.accumulator.SessionAggrStatAccumulator;
import com.wen.spark.project.session.bean.SessionFactory;
import com.wen.spark.project.session.conf.ConfigurationManager;
import com.wen.spark.project.session.conf.Constants;
import com.wen.spark.project.session.dao.ISessionAggrStatDAO;
import com.wen.spark.project.session.dao.ITaskDAO;
import com.wen.spark.project.session.domain.SessionAggrStat;
import com.wen.spark.project.session.entrty.Task;
import com.wen.spark.project.session.factory.DAOFactory;
import com.wen.spark.project.session.util.*;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import com.alibaba.fastjson.JSONObject;
import org.bouncycastle.crypto.tls.SRTPProtectionProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;


/**
 * 用户访问session分析Spark作业
 *
 * @author Administrator
 */

/**
 * 用户访问session分析Spark作业
 *
 * 接收用户创建的分析任务，用户可能指定的条件如下：
 *
 * 1、时间范围：起始日期~结束日期
 * 2、性别：男或女
 * 3、年龄范围
 * 4、职业：多选
 * 5、城市：多选
 * 6、搜索词：多个搜索词，只要某个session中的任何一个action搜索过指定的关键词，那么session就符合条件
 * 7、点击品类：多个品类，只要某个session中的任何一个action点击过某个品类，那么session就符合条件
 *
 * 我们的spark作业如何接受用户创建的任务？
 *
 * J2EE平台在接收用户创建任务的请求之后，会将任务信息插入MySQL的task表中，任务参数以JSON格式封装在task_param
 * 字段中
 *
 * 接着J2EE平台会执行我们的spark-submit shell脚本，并将taskid作为参数传递给spark-submit shell脚本
 * spark-submit shell脚本，在执行时，是可以接收参数的，并且会将接收的参数，传递给Spark作业的main函数
 * 参数就封装在main函数的args数组中
 *
 * 这是spark本身提供的特性
 *
 * @author Administrator
 *
 */
public class UserVisitSessionAnalyzeSpark {

    static Logger logger = LoggerFactory.getLogger(UserVisitSessionAnalyzeSpark.class);
    static long taskid = 64350166152L;

    public static void main(String[] args) {
        // 构建Spark上下文
        SparkConf conf = new SparkConf()
                .setAppName(Constants.SPARK.SPARK_APP_NAME_SESSION)
                .setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = getSQLContext(sc.sc());

        // 生成模拟测试数据
        mockData(sc, sqlContext);

        // 创建需要使用的DAO组件
        ITaskDAO taskDAO = DAOFactory.getTaskDAO();

        // 首先得查询出来指定的任务，并获取任务的查询参数
        // long taskid = ParamUtils.getTaskIdFromArgs(args);
        setTask();
        Task task = taskDAO.findById(taskid);

        JSONObject taskParam = JSONObject.parseObject(task.getTaskParam());



        // 如果要进行session粒度的数据聚合
        // 首先要从user_visit_action表中，查询出来指定日期范围内的行为数据
        JavaRDD<Row> actionRDD = getActionRDDByDateRange(sqlContext, taskParam);

        // 首先，可以将行为数据，按照session_id进行groupByKey分组
        // 此时的数据的粒度就是session粒度了，然后呢，可以将session粒度的数据
        // 与用户信息数据，进行join
        // 然后就可以获取到session粒度的数据，同时呢，数据里面还包含了session对应的user的信息
        JavaPairRDD<String, String> sessionid2AggrInfoRDD =
                aggregateBySession(sqlContext, actionRDD);
        //重构同时进行过滤统计
        Accumulator<String> sessionAggrStatAccumulator=sc.accumulator("",new SessionAggrStatAccumulator());

        JavaPairRDD<String, String> fliterRDD= filterSessionAndAggrStat(sessionAggrStatAccumulator,sessionid2AggrInfoRDD,taskParam);

        /**
         * 计算出各个范围的session 占比写入mysql
         *
         */

        //        fliterRDD.foreach(new VoidFunction<Tuple2<String, String>>() {
//            @Override
//            public void call(Tuple2<String, String> stringStringTuple2) throws Exception {
//                System.out.println(stringStringTuple2._1 + " " + stringStringTuple2._2);
//            }
//        });
        System.out.println(fliterRDD.count());
        randomExtractSession(fliterRDD);

        calculateAndPersistAggrStat(sessionAggrStatAccumulator.value ());

        // 关闭Spark上下文
        sc.close();
    }

    private static  void setTask() {
        Map<String, String> map = new HashMap();
        map.put("startDate", "2018-05-01");
        map.put("endDate", "2018-06-31");

        String sql = "insert into task(task_id,task_name,task_param) values('" + taskid + "','test01','" + JSON.toJSONString(map) + "')";
        SessionFactory sessionFactory = SessionFactory.getSessionFactory();
        try {
            sessionFactory.executeUpdate(sql, null);
        }catch (Exception e){
             sql = "update task set task_param='" + JSON.toJSONString(map) + "'";
            sessionFactory.executeUpdate(sql, null);

        }
    }

    /**
     * 获取SQLContext
     * 如果是在本地测试环境的话，那么就生成SQLContext对象
     * 如果是在生产环境运行的话，那么就生成HiveContext对象
     * @param sc SparkContext
     * @return SQLContext
     */
    private static SQLContext getSQLContext(SparkContext sc) {
        boolean local = GetValueUtils.getBoolean(ConfigurationManager.getProperty(Constants.SPARK.SPARK_LOCAL));
        if (local) {
            return new SQLContext(sc);
        } else {
            return new HiveContext(sc);
        }
    }


    /**
     * 生成模拟数据（只有本地模式，才会去生成模拟数据）
     * @param sc
     * @param sqlContext
     */
    private static void mockData(JavaSparkContext sc, SQLContext sqlContext) {
        boolean local = GetValueUtils.getBoolean(ConfigurationManager.getProperty(Constants.SPARK.SPARK_LOCAL));
        if (local) {
            MockData.mock(sc, sqlContext);
        }
    }


    /**
     * 获取指定日期范围内的用户访问行为数据
     * @param sqlContext SQLContext
     * @param taskParam 任务参数
     * @return 行为数据RDD
     */
    private static JavaRDD<Row> getActionRDDByDateRange(
            SQLContext sqlContext, JSONObject taskParam) {
        String startDate = ParamUtils.getParam(taskParam, Constants.SESSION_PROJECT.PARAM_START_DATE);
        String endDate = ParamUtils.getParam(taskParam, Constants.SESSION_PROJECT.PARAM_END_DATE);

        String sql =
                "select * "
                        + "from user_visit_action "
                        + "where date>='" + startDate + "' "
                        + "and date<='" + endDate + "'";

        Dataset actionDF = sqlContext.sql(sql);

        return actionDF.javaRDD();
    }

    /**
     * 对行为数据按session粒度进行聚合
     * @param actionRDD 行为数据RDD
     * @return session粒度聚合数据
     */
    private static JavaPairRDD<String, String> aggregateBySession(SQLContext sqlContext, JavaRDD<Row> actionRDD) {
        // 现在actionRDD中的元素是Row，一个Row就是一行用户访问行为记录，比如一次点击或者搜索
        // 我们现在需要将这个Row映射成<sessionid,Row>的格式
        JavaPairRDD<String, Row> sessionid2ActionRDD = actionRDD.mapToPair(

                /**
                 * PairFunction
                 * 第一个参数，相当于是函数的输入
                 * 第二个参数和第三个参数，相当于是函数的输出（Tuple），分别是Tuple第一个和第二个值
                 */
                new PairFunction<Row, String, Row>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<String, Row> call(Row row) throws Exception {
                        return new Tuple2<String, Row>(row.getString(2), row);
                    }

                });

        // 对行为数据按session粒度进行分组
        JavaPairRDD<String, Iterable<Row>> sessionid2ActionsRDD =
                sessionid2ActionRDD.groupByKey();

        // 对每一个session分组进行聚合，将session中所有的搜索词和点击品类都聚合起来
        // 到此为止，获取的数据格式，如下：<userid,partAggrInfo(sessionid,searchKeywords,clickCategoryIds)>
        JavaPairRDD<Long, String> userid2PartAggrInfoRDD = sessionid2ActionsRDD.mapToPair(

                new PairFunction<Tuple2<String, Iterable<Row>>, Long, String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<Long, String> call(Tuple2<String, Iterable<Row>> tuple)
                            throws Exception {
                        String sessionid = tuple._1;
                        Iterator<Row> iterator = tuple._2.iterator();

                        StringBuffer searchKeywordsBuffer = new StringBuffer("");
                        StringBuffer clickCategoryIdsBuffer = new StringBuffer("");

                        Long userid = null;
                       // session的起始和结束时间
                        Date startTime = null;
                        Date endTime = null;
                        // session的访问步长
                        int stepLength = 0;
                        // 遍历session所有的访问行为
                        while (iterator.hasNext()) {
                            // 提取每个访问行为的搜索词字段和点击品类字段
                            Row row = iterator.next();
                            if (userid == null) {
                                userid = row.getLong(1);
                            }
                            String searchKeyword = row.getString(5);
                            Long clickCategoryId = null;
                            if (row.get(6) != null) {
                                clickCategoryId = row.getLong(6);
                            }

                            // 计算session开始和结束时间
                            Date actionTime = DateUtils.parseTime(row.getString(4));

                            if(startTime == null) {
                                startTime = actionTime;
                            }
                            if(endTime == null) {
                                endTime = actionTime;
                            }

                            if(actionTime.before(startTime)) {
                                startTime = actionTime;
                            }
                            if(actionTime.after(endTime)) {
                                endTime = actionTime;
                            }
                            // 计算session访问步长
                            stepLength++;
                            // 实际上这里要对数据说明一下
                            // 并不是每一行访问行为都有searchKeyword何clickCategoryId两个字段的
                            // 其实，只有搜索行为，是有searchKeyword字段的
                            // 只有点击品类的行为，是有clickCategoryId字段的
                            // 所以，任何一行行为数据，都不可能两个字段都有，所以数据是可能出现null值的

                            // 我们决定是否将搜索词或点击品类id拼接到字符串中去
                            // 首先要满足：不能是null值
                            // 其次，之前的字符串中还没有搜索词或者点击品类id

                            if (StringUtils.isNotEmpty(searchKeyword)) {
                                if (!searchKeywordsBuffer.toString().contains(searchKeyword)) {
                                    searchKeywordsBuffer.append(searchKeyword + ",");
                                }
                            }
                            if (clickCategoryId != null) {
                                if (!clickCategoryIdsBuffer.toString().contains(
                                        String.valueOf(clickCategoryId))) {
                                    clickCategoryIdsBuffer.append(clickCategoryId + ",");
                                }
                            }
                        }


                        String searchKeywords = StringUtils.trimComma(searchKeywordsBuffer.toString());
                        String clickCategoryIds = StringUtils.trimComma(clickCategoryIdsBuffer.toString());

                        // 计算session访问时长（秒）
                        long visitLength = (endTime.getTime() - startTime.getTime()) / 1000;
                        // 我们返回的数据格式，即使<sessionid,partAggrInfo>
                        // 但是，这一步聚合完了以后，其实，我们是还需要将每一行数据，跟对应的用户信息进行聚合
                        // 问题就来了，如果是跟用户信息进行聚合的话，那么key，就不应该是sessionid
                        // 就应该是userid，才能够跟<userid,Row>格式的用户信息进行聚合
                        // 如果我们这里直接返回<sessionid,partAggrInfo>，还得再做一次mapToPair算子
                        // 将RDD映射成<userid,partAggrInfo>的格式，那么就多此一举

                        // 所以，我们这里其实可以直接，返回的数据格式，就是<userid,partAggrInfo>
                        // 然后跟用户信息join的时候，将partAggrInfo关联上userInfo
                        // 然后再直接将返回的Tuple的key设置成sessionid
                        // 最后的数据格式，还是<sessionid,fullAggrInfo>

                        // 聚合数据，用什么样的格式进行拼接？
                        // 我们这里统一定义，使用key=value|key=value
                        String partAggrInfo = Constants.SESSION_PROJECT.FIELD_SESSION_ID + "=" + sessionid + "|"
                                + Constants.SESSION_PROJECT.FIELD_SEARCH_KEYWORDS + "=" + searchKeywords + "|"
                                + Constants.SESSION_PROJECT.FIELD_CLICK_CATEGORY_IDS + "=" + clickCategoryIds+ "|"
                                + Constants.SESSION_PROJECT.FIELD_VISIT_LENGTH + "=" + visitLength + "|"
                                + Constants.SESSION_PROJECT.FIELD_STEP_LENGTH + "=" + stepLength + "|"
                                 + Constants.SESSION_PROJECT.FIELD_START_TIME + "=" + DateUtils.formatTime(startTime);

                        return new Tuple2<Long, String>(userid, partAggrInfo);
                    }

                });
        // 查询所有用户数据，并映射成<userid,Row>的格式
        String sql = "select * from user_info";
        JavaRDD<Row> userInfoRDD = sqlContext.sql(sql).javaRDD();

        JavaPairRDD<Long, Row> userid2InfoRDD = userInfoRDD.mapToPair(

                new PairFunction<Row, Long, Row>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<Long, Row> call(Row row) throws Exception {
                        return new Tuple2<Long, Row>(row.getLong(0), row);
                    }

                });

        // 将session粒度聚合数据，与用户信息进行join
        JavaPairRDD<Long, Tuple2<String, Row>> userid2FullInfoRDD =
                userid2PartAggrInfoRDD.join(userid2InfoRDD);

        // 对join起来的数据进行拼接，并且返回<sessionid,fullAggrInfo>格式的数据
        JavaPairRDD<String, String> sessionid2FullAggrInfoRDD = userid2FullInfoRDD.mapToPair(

                new PairFunction<Tuple2<Long, Tuple2<String, Row>>, String, String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<String, String> call(
                            Tuple2<Long, Tuple2<String, Row>> tuple)
                            throws Exception {
                        String partAggrInfo = tuple._2._1;
                        Row userInfoRow = tuple._2._2;

                        String sessionid = StringUtils.getFieldFromConcatString(
                                partAggrInfo, "\\|", Constants.SESSION_PROJECT.FIELD_SESSION_ID);

                        int age = userInfoRow.getInt(3);
                        String professional = userInfoRow.getString(4);
                        String city = userInfoRow.getString(5);
                        String sex = userInfoRow.getString(6);

                        String fullAggrInfo = partAggrInfo + "|"
                                + Constants.FIELD.FIELD_AGE + "=" + age + "|"
                                + Constants.FIELD.FIELD_PROFESSIONAL + "=" + professional + "|"
                                + Constants.FIELD.FIELD_CITY + "=" + city + "|"
                                + Constants.FIELD.FIELD_SEX + "=" + sex;

                        return new Tuple2<String, String>(sessionid, fullAggrInfo);
                    }

                });

        return sessionid2FullAggrInfoRDD;
    }

    /**
     * 对集合的session  进行过滤 并进行聚合统计
     * @param sessionid2AggrInfoRDD
     * @return
     */
    private static JavaPairRDD<String, String> filterSessionAndAggrStat(Accumulator<String> sessionAggrStatAccumulator,
            JavaPairRDD<String, String> sessionid2AggrInfoRDD,
            final JSONObject taskParam) {
        // 为了使用我们后面的ValieUtils，所以，首先将所有的筛选参数拼接成一个连接串
        // 此外，这里其实大家不要觉得是多此一举
        // 其实我们是给后面的性能优化埋下了一个伏笔
        String startAge = ParamUtils.getParam(taskParam, Constants.SESSION_PROJECT.PARAM_STARTAGE);
        String endAge = ParamUtils.getParam(taskParam, Constants.SESSION_PROJECT.PARAM_ENDAGE);
        String professionals = ParamUtils.getParam(taskParam, Constants.SESSION_PROJECT.PARAM_PROFESSIONALS);
        String cities = ParamUtils.getParam(taskParam, Constants.SESSION_PROJECT.PARAM_CITIES);
        String sex = ParamUtils.getParam(taskParam, Constants.SESSION_PROJECT.PARAM_SEX);
        String keywords = ParamUtils.getParam(taskParam, Constants.SESSION_PROJECT.PARAM_KEYWORDS);
        String categoryIds = ParamUtils.getParam(taskParam, Constants.SESSION_PROJECT.PARAM_CATEGORY_IDS);

        String _parameter = (startAge != null ? Constants.SESSION_PROJECT.PARAM_STARTAGE + "=" + startAge + "|" : "")
                + (endAge != null ? Constants.SESSION_PROJECT.PARAM_ENDAGE + "=" + endAge + "|" : "")
                + (professionals != null ? Constants.SESSION_PROJECT.PARAM_PROFESSIONALS + "=" + professionals + "|" : "")
                + (cities != null ? Constants.SESSION_PROJECT.PARAM_CITIES + "=" + cities + "|" : "")
                + (sex != null ? Constants.SESSION_PROJECT.PARAM_SEX + "=" + sex + "|" : "")
                + (keywords != null ? Constants.SESSION_PROJECT.PARAM_KEYWORDS + "=" + keywords + "|" : "")
                + (categoryIds != null ? Constants.SESSION_PROJECT.PARAM_CATEGORY_IDS + "=" + categoryIds: "");

        if(_parameter.endsWith("\\|")) {
            _parameter = _parameter.substring(0, _parameter.length() - 1);
        }

        final String parameter = _parameter;

        // 根据筛选参数进行过滤
        JavaPairRDD<String, String> filteredSessionid2AggrInfoRDD = sessionid2AggrInfoRDD.filter(

                new Function<Tuple2<String,String>, Boolean>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Boolean call(Tuple2<String, String> tuple) throws Exception {
                        // 首先，从tuple中，获取聚合数据
                        String aggrInfo = tuple._2;

                        // 接着，依次按照筛选条件进行过滤
                        // 按照年龄范围进行过滤（startAge、endAge）
                        if(!ValidUtils.between(aggrInfo, Constants.FIELD.FIELD_AGE,
                                parameter, Constants.SESSION_PROJECT.PARAM_STARTAGE, Constants.SESSION_PROJECT.PARAM_ENDAGE)) {
                            return false;
                        }

                        // 按照职业范围进行过滤（professionals）
                        // 互联网,IT,软件
                        // 互联网
                        if(!ValidUtils.in(aggrInfo, Constants.FIELD.FIELD_PROFESSIONAL,
                                parameter, Constants.SESSION_PROJECT.PARAM_PROFESSIONALS)) {
                            return false;
                        }

                        // 按照城市范围进行过滤（cities）
                        // 北京,上海,广州,深圳
                        // 成都
                        if(!ValidUtils.in(aggrInfo, Constants.FIELD.FIELD_CITY,
                                parameter, Constants.SESSION_PROJECT.PARAM_CITIES)) {
                            return false;
                        }

                        // 按照性别进行过滤
                        // 男/女
                        // 男，女
                        if(!ValidUtils.equal(aggrInfo, Constants.FIELD.FIELD_SEX,
                                parameter, Constants.SESSION_PROJECT.PARAM_SEX)) {
                            return false;
                        }

                        // 按照搜索词进行过滤
                        // 我们的session可能搜索了 火锅,蛋糕,烧烤
                        // 我们的筛选条件可能是 火锅,串串香,iphone手机
                        // 那么，in这个校验方法，主要判定session搜索的词中，有任何一个，与筛选条件中
                        // 任何一个搜索词相当，即通过
                        if(!ValidUtils.in(aggrInfo, Constants.SESSION_PROJECT.FIELD_SEARCH_KEYWORDS,
                                parameter, Constants.SESSION_PROJECT.PARAM_KEYWORDS)) {
                            return false;
                        }

                        // 按照点击品类id进行过滤
                        if(!ValidUtils.in(aggrInfo, Constants.SESSION_PROJECT.FIELD_CLICK_CATEGORY_IDS,
                                parameter, Constants.SESSION_PROJECT.PARAM_CATEGORY_IDS)) {
                            return false;
                        }
                        //如果经过之前的多个过滤条件，程序能够走到这里
                        //那么就说明，该session 是通过了用户指定的筛选条件的，也就是合法的数据
                        //根据session  那么就要退session的访问时长和访问步长进行统计
                        //根据session  的范围进行累加操作。
                        sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.SESSION_COUNT );
                        //  计算出访问时长和访问步长进行累加

                        long visitLength=Long.parseLong (  StringUtils.getFieldFromConcatString ( aggrInfo,"\\|",Constants.SESSION_PROJECT.FIELD_VISIT_LENGTH ));
                        long stepLength=Long.parseLong (  StringUtils.getFieldFromConcatString ( aggrInfo,"\\|",Constants.SESSION_PROJECT.FIELD_STEP_LENGTH));
                        calculateVisitLength(visitLength);
                        calculateStepLength(stepLength);
                        return true;
                    }

                    /**
                     * 计算访问时长
                     * @param visitLength
                     */
                    private void calculateVisitLength(long visitLength){
                        if(visitLength>=1&&visitLength<=3){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.TIME_PERIOD_1s_3s );
                        }
                        if(visitLength>=4&&visitLength<=6){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.TIME_PERIOD_4s_6s );
                        }
                        if(visitLength>=7&&visitLength<=9){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.TIME_PERIOD_7s_9s );
                        }
                        if(visitLength>=10&&visitLength<=30){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.TIME_PERIOD_10s_30s );
                        }
                        if(visitLength>30&&visitLength<=60){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.TIME_PERIOD_30s_60s );
                        }
                        if(visitLength>60&&visitLength<=180){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.TIME_PERIOD_1m_3m );
                        }
                        if(visitLength>180&&visitLength<=600){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.TIME_PERIOD_3m_10m );
                        }
                        if(visitLength>600&&visitLength<=1800){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.TIME_PERIOD_10m_30m );
                        }
                        if(visitLength>1800){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.TIME_PERIOD_30m );
                        }
                    }
                    private void calculateStepLength(long stepLength){
                        if(stepLength>=1&&stepLength<=3){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.STEP_PERIOD_1_3 );
                        }
                        if(stepLength>=4&&stepLength<=6){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.STEP_PERIOD_4_6 );
                        }
                        if(stepLength>=7&&stepLength<=9){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.STEP_PERIOD_7_9 );
                        }
                        if(stepLength>=10&&stepLength<=30){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.STEP_PERIOD_10_30 );
                        }
                        if(stepLength>30&&stepLength<=60){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.STEP_PERIOD_30_60 );
                        }
                        if(stepLength>60){
                            sessionAggrStatAccumulator.add ( Constants.SESSION_PROJECT.STEP_PERIOD_60 );
                        }

                    }

                });

        return filteredSessionid2AggrInfoRDD;
    }

    /**
     * 计算各个session  占比并写入mysql
     *
     * @param value
     */
    private static void calculateAndPersistAggrStat(String value) {
        //从Accumulator统计串中获取值
        long session_count = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.SESSION_COUNT));

        long visit_length_1s_3s = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.TIME_PERIOD_1s_3s));
        long visit_length_4s_6s = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.TIME_PERIOD_4s_6s));
        long visit_length_7s_9s = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.TIME_PERIOD_7s_9s));
        long visit_length_10s_30s = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.TIME_PERIOD_10s_30s));
        long visit_length_30s_60s = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.TIME_PERIOD_30s_60s));
        long visit_length_1m_3m = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.TIME_PERIOD_1m_3m));
        long visit_length_3m_10m = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.TIME_PERIOD_3m_10m));
        long visit_length_10m_30m = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.TIME_PERIOD_10m_30m));
        long visit_length_30m = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.TIME_PERIOD_30m));

        long step_length_1_3 = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.STEP_PERIOD_1_3));
        long step_length_4_6 = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.STEP_PERIOD_4_6));
        long step_length_7_9 = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.STEP_PERIOD_7_9));
        long step_length_10_30 = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.STEP_PERIOD_10_30));
        long step_length_30_60 = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.STEP_PERIOD_30_60));
        long step_length_60 = Long.valueOf(StringUtils.getFieldFromConcatString(
                value, "\\|", Constants.SESSION_PROJECT.STEP_PERIOD_60));

        //计算各个访问时长和访问步长的范围
        double visit_length_1s_3s_ratio = NumberUtils.formatDouble(
                (double)visit_length_1s_3s / (double)session_count, 2);
        double visit_length_4s_6s_ratio = NumberUtils.formatDouble(
                (double)visit_length_4s_6s / (double)session_count, 2);
        double visit_length_7s_9s_ratio = NumberUtils.formatDouble(
                (double)visit_length_7s_9s / (double)session_count, 2);
        double visit_length_10s_30s_ratio = NumberUtils.formatDouble(
                (double)visit_length_10s_30s / (double)session_count, 2);
        double visit_length_30s_60s_ratio = NumberUtils.formatDouble(
                (double)visit_length_30s_60s / (double)session_count, 2);
        double visit_length_1m_3m_ratio = NumberUtils.formatDouble(
                (double)visit_length_1m_3m / (double)session_count, 2);
        double visit_length_3m_10m_ratio = NumberUtils.formatDouble(
                (double)visit_length_3m_10m / (double)session_count, 2);
        double visit_length_10m_30m_ratio = NumberUtils.formatDouble(
                (double)visit_length_10m_30m / (double)session_count, 2);
        double visit_length_30m_ratio = NumberUtils.formatDouble(
                (double)visit_length_30m / (double)session_count, 2);

        double step_length_1_3_ratio = NumberUtils.formatDouble(
                (double)step_length_1_3 / (double)session_count, 2);
        double step_length_4_6_ratio = NumberUtils.formatDouble(
                (double)step_length_4_6 / (double)session_count, 2);
        double step_length_7_9_ratio = NumberUtils.formatDouble(
                (double)step_length_7_9 / (double)session_count, 2);
        double step_length_10_30_ratio = NumberUtils.formatDouble(
                (double)step_length_10_30 / (double)session_count, 2);
        double step_length_30_60_ratio = NumberUtils.formatDouble(
                (double)step_length_30_60 / (double)session_count, 2);
        double step_length_60_ratio = NumberUtils.formatDouble(
                (double)step_length_60 / (double)session_count, 2);

        //将访问结果封装成Domain对象
        SessionAggrStat sessionAggrStat = new SessionAggrStat();
        sessionAggrStat.setTaskid(taskid);
        sessionAggrStat.setSession_count(session_count);
        sessionAggrStat.setVisit_length_1s_3s_ratio(visit_length_1s_3s_ratio);
        sessionAggrStat.setVisit_length_4s_6s_ratio(visit_length_4s_6s_ratio);
        sessionAggrStat.setVisit_length_7s_9s_ratio(visit_length_7s_9s_ratio);
        sessionAggrStat.setVisit_length_10s_30s_ratio(visit_length_10s_30s_ratio);
        sessionAggrStat.setVisit_length_30s_60s_ratio(visit_length_30s_60s_ratio);
        sessionAggrStat.setVisit_length_1m_3m_ratio(visit_length_1m_3m_ratio);
        sessionAggrStat.setVisit_length_3m_10m_ratio(visit_length_3m_10m_ratio);
        sessionAggrStat.setVisit_length_10m_30m_ratio(visit_length_10m_30m_ratio);
        sessionAggrStat.setVisit_length_30m_ratio(visit_length_30m_ratio);

        sessionAggrStat.setStep_length_1_3_ratio(step_length_1_3_ratio);
        sessionAggrStat.setStep_length_4_6_ratio(step_length_4_6_ratio);
        sessionAggrStat.setStep_length_7_9_ratio(step_length_7_9_ratio);
        sessionAggrStat.setStep_length_10_30_ratio(step_length_10_30_ratio);
        sessionAggrStat.setStep_length_30_60_ratio(step_length_30_60_ratio);
        sessionAggrStat.setStep_length_60_ratio(step_length_60_ratio);

        //调用对应的DAO插入统计结果
        ISessionAggrStatDAO sessionAggrStatDAO = DAOFactory.getSessionAggrStatDAO();
        sessionAggrStatDAO.insert(sessionAggrStat);
    }


    /**
     * 随机抽取session
     * @param sessionid2AggrInfoRDD
     */
    private static void randomExtractSession(
            JavaPairRDD<String, String> sessionid2AggrInfoRDD) {
        // 第一步，计算出每天每小时的session数量，获取<yyyy-MM-dd_HH,sessionid>格式的RDD
        JavaPairRDD<String, String> time2sessionidRDD = sessionid2AggrInfoRDD.mapToPair(

                new PairFunction<Tuple2<String,String>, String, String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<String, String> call(
                            Tuple2<String, String> tuple) throws Exception {
                        String aggrInfo = tuple._2;

                        String startTime = StringUtils.getFieldFromConcatString(
                                aggrInfo, "\\|", Constants.SESSION_PROJECT.FIELD_START_TIME);
                        String dateHour = DateUtils.getDateHour(startTime);

                        return new Tuple2<String, String>(dateHour, aggrInfo);
                    }

                });

        /**
         * 思考一下：这里我们不要着急写大量的代码，做项目的时候，一定要用脑子多思考
         *
         * 每天每小时的session数量，然后计算出每天每小时的session抽取索引，遍历每天每小时session
         * 首先抽取出的session的聚合数据，写入session_random_extract表
         * 所以第一个RDD的value，应该是session聚合数据
         *
         */

        // 得到每天每小时的session数量
        Map<String, Long> countMap = time2sessionidRDD.countByKey();

        // 第二步，使用按时间比例随机抽取算法，计算出每天每小时要抽取session的索引

        // 将<yyyy-MM-dd_HH,count>格式的map，转换成<yyyy-MM-dd,<HH,count>>的格式
        Map<String, Map<String, Long>> dateHourCountMap =
                new HashMap<String, Map<String, Long>>();

        for(Map.Entry<String, Long> countEntry : countMap.entrySet()) {
            String dateHour = countEntry.getKey();
            String date = dateHour.split("_")[0];
            String hour = dateHour.split("_")[1];

            long count = Long.valueOf(String.valueOf(countEntry.getValue()));

            Map<String, Long> hourCountMap = dateHourCountMap.get(date);
            if(hourCountMap == null) {
                hourCountMap = new HashMap<String, Long>();
                dateHourCountMap.put(date, hourCountMap);
            }

            hourCountMap.put(hour, count);
        }

        // 开始实现我们的按时间比例随机抽取算法

        // 总共要抽取100个session，先按照天数，进行平分
        int extractNumberPerDay = 100 / dateHourCountMap.size();

        // <date,<hour,(3,5,20,102)>>
        Map<String, Map<String, List<Integer>>> dateHourExtractMap =
                new HashMap<String, Map<String, List<Integer>>>();

        Random random = new Random();

        for(Map.Entry<String, Map<String, Long>> dateHourCountEntry : dateHourCountMap.entrySet()) {
            String date = dateHourCountEntry.getKey();
            Map<String, Long> hourCountMap = dateHourCountEntry.getValue();

            // 计算出这一天的session总数
            long sessionCount = 0L;
            for(long hourCount : hourCountMap.values()) {
                sessionCount += hourCount;
            }

            Map<String, List<Integer>> hourExtractMap = dateHourExtractMap.get(date);
            if(hourExtractMap == null) {
                hourExtractMap = new HashMap<String, List<Integer>>();
                dateHourExtractMap.put(date, hourExtractMap);
            }

            // 遍历每个小时
            for(Map.Entry<String, Long> hourCountEntry : hourCountMap.entrySet()) {
                String hour = hourCountEntry.getKey();
                long count = hourCountEntry.getValue();

                // 计算每个小时的session数量，占据当天总session数量的比例，直接乘以每天要抽取的数量
                // 就可以计算出，当前小时需要抽取的session数量
                int hourExtractNumber = (int)(((double)count / (double)sessionCount)
                        * extractNumberPerDay);
                if(hourExtractNumber > count) {
                    hourExtractNumber = (int) count;
                }

                // 先获取当前小时的存放随机数的list
                List<Integer> extractIndexList = hourExtractMap.get(hour);
                if(extractIndexList == null) {
                    extractIndexList = new ArrayList<Integer>();
                    hourExtractMap.put(hour, extractIndexList);
                }

                // 生成上面计算出来的数量的随机数
                for(int i = 0; i < hourExtractNumber; i++) {
                    int extractIndex = random.nextInt((int) count);
                    while(extractIndexList.contains(extractIndex)) {
                        extractIndex = random.nextInt((int) count);
                    }
                    extractIndexList.add(extractIndex);
                }
            }
        }
    }
}
