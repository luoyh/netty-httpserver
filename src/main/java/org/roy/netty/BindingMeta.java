package org.roy.netty;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * <p>Created: Feb 22, 2018 9:58:23 AM</p>
 * 
 * @author luoyh(Roy)
 * @version 1.0
 * @since 1.7
 */
public class BindingMeta {
    
    private Map<String, Method> apiMap;             // key=path, value=method
    private Map<Class<?>, Object> beanMap;          // key=class, value=class instance
    private Map<String, String[]> paramNamesMap;    // key=path, value=method parameter names
    private String packageName;
    private volatile boolean initialize = false;
    
    public BindingMeta(String packageName) {
        if (StringUtils.isBlank(packageName)) {
            throw new IllegalArgumentException("packageName must be not null");
        }
        this.packageName = packageName;
        apiMap = Maps.newHashMap();
        beanMap = Maps.newHashMap();
        paramNamesMap = Maps.newHashMap();
    }
    
    public void init() throws Exception {
        if (initialize) return; 
        initialize = true;
        ImmutableSet<ClassInfo> classInfos = ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(packageName);
        for (ClassInfo classInfo : classInfos) {
            Class<?> clazz = Class.forName(classInfo.getName());
            Rest rest = clazz.getDeclaredAnnotation(Rest.class);
            if (null == rest) {
                continue;
            }
            String clazzPath = rest.value();
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Rest mRest = method.getDeclaredAnnotation(Rest.class);
                if (null != mRest) {
                    String path = clazzPath + mRest.value();
                    apiMap.put(path, method);
                    beanMap.put(clazz, clazz.newInstance());
                    Parameter[] params = method.getParameters();
                    if (params.length > 0) {
                        String[] paramNames = new String[params.length];
                        for (int i = 0; i < params.length; i ++) {
                            paramNames[i] = params[i].getName();
                        }
                        paramNamesMap.put(path, paramNames);
                    }
                }
            }
        }
    }
    
    public Object invoke(HttpRequest request) throws Exception {
        init(); // in #init() checked
        
        QueryStringDecoder qsd = new QueryStringDecoder(request.uri());
        String path = qsd.path();
        Method method = apiMap.get(path);
        if (null == method) {
            throw new RuntimeException("404 not found");
        }
        String[] paramNames = paramNamesMap.get(path);
        if (null == paramNames) { // no parameter
            return method.invoke(beanMap.get(method.getDeclaringClass()));
        }
        Class<?>[] paramTypes = method.getParameterTypes();
        Map<String, List<String>> params = qsd.parameters();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i ++) {
            Class<?> clazz = paramTypes[i];
            String name = paramNames[i];
            List<String> p = params.get(name);
            if (null == p || p.isEmpty()) {
                args[i] = null;
                continue;
            }
            String one = p.get(0);
            if (null == one) {
                args[i] = null;
            } else if (int.class == clazz || Integer.class == clazz) {
                args[i] = Integer.valueOf(one);
            } else if (float.class == clazz || Float.class == clazz) {
                args[i] = Float.valueOf(one);
            } else if (double.class == clazz || Double.class == clazz) {
                args[i] = Double.valueOf(one);
            } else if (long.class == clazz || Long.class == clazz) {
                args[i] = Long.valueOf(one);
            } else if (String.class == clazz) { 
                args[i] = one;
            } else { // not support
                throw new IllegalArgumentException(method.getName() + " parameter type " + clazz.getName() + " not support");
            }
        }
        return method.invoke(beanMap.get(method.getDeclaringClass()), args);
    }

}
