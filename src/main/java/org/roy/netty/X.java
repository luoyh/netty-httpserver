package org.roy.netty;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * <p>Created: Feb 7, 2018 2:49:49 PM</p>
 * 
 * @author luoyh(Roy)
 * @version 1.0
 * @since 1.7
 */
public class X {

    public static void main(String[] args) throws Exception {
//        Method[] methods = X.class.getDeclaredMethods();
//        for (Method method : methods) {
//            System.out.println(method);
//            Parameter[] params = method.getParameters();
//            System.err.println(params.length);
//            for (Parameter param : params) {
//                System.out.println(param.getName());
//            }
//        }
        
        System.err.println("===============");
        
        ImmutableSet<ClassInfo> clazzInfo = ClassPath.from(X.class.getClassLoader())
                .getTopLevelClassesRecursive("org.roy.netty");
                //.getTopLevelClasses("org.roy.netty");
        for (ClassInfo ci : clazzInfo) {
            System.out.println(Class.forName(ci.getName()));
        }
    }
    
    
    public void idd() {
        System.out.println("hello");
    }
    
}
