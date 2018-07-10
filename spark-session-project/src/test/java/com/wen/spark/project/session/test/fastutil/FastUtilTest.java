package com.wen.spark.project.session.test.fastutil;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.HashMap;
import java.util.Map;
/**
 * @author : WChen129
 * @date : 2018-06-23
 */
public class FastUtilTest {
    public static void main(String[] args) {
        Map<String,IntList> map=new HashMap<> (  );
        IntList list=new IntArrayList (  );
        list.add ( 21212121 );
        map.put ( "test",list );
        list=map.get ( "test" );
        System.out.println (list.get ( 0 ));
    }
}
