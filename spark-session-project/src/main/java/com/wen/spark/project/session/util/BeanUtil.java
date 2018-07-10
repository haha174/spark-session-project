package com.wen.spark.project.session.util;

import com.wen.spark.project.session.annotation.FieldName;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class BeanUtil {

    private static Map<String, Field[]> fieldCache = new ConcurrentHashMap<>();


    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) {
        T entry = null;
        try {
            entry = (T) clazz.getDeclaredConstructor(Map.class).newInstance(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entry;
    }

    public static String stringTrim(String str) {
        if (StringUtils.isNotEmpty(str)) {
            return str.trim();
        }
        return str;
    }

    public static <T> T mapToBean( Class<T> clazz,Map<String, Object> map) {
        T instance = null;
        try {
            instance = clazz.newInstance();
            Field[] fields = fieldCache.get(clazz.getName());
            if (null == fields) {
                fields = clazz.getDeclaredFields();
                fieldCache.put(clazz.getName(), fields);
            }
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                field.setAccessible(true);
                String fieldName=field.getName();
                FieldName fieldNameAnnotation=field.getAnnotation(FieldName.class);
                if(fieldNameAnnotation!=null){
                    fieldName=fieldNameAnnotation.value();
                }
                if(map.containsKey(fieldName)){
                    field.set(instance, map.get(fieldName));
                }
                field.setAccessible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public static  long getTaskId(int num){
        StringBuffer sb=new StringBuffer (  );
        for (int i=0;i<num;i++){
            sb.append ( (int)(Math.random ()*10) );
        }
        return  Long.parseLong ( sb.toString () );
    }

}
