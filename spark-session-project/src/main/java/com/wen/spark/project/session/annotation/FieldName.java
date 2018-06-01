package com.wen.spark.project.session.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  数据库中字段一般和 model
 *  字段名称不一样 所以这里定义一个主机来转换名称
 *  只需要将名称不一样的注解进行转换即可
 * @author : WChen129
 * @date : 2018-05-24
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldName {
    String value();
}
