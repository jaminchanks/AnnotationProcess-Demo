package com.rhythm7.lib_api;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Jaminchanks on 2018-02-10.
 */

public class RandomUtil {
    private static final String RANDOM_SUFFIX = "_Random";

    private RandomUtil() {

    }

    /**
     * 通过反射的方式找到对应的辅助类，并调用对应的方法实现属性的注入
     * @param object 被注入的对象
     */
    public static void inject(Object object) {
        try {
            Class bindingClass = Class.forName(object.getClass().getCanonicalName() + RANDOM_SUFFIX);
            //noinspection unchecked
            Constructor constructor = bindingClass.getConstructor(object.getClass());
            constructor.newInstance(object);

        } catch (ClassNotFoundException e) {
            Log.e("TAG", "Meaningful Message", e);
        } catch (NoSuchMethodException e) {
            Log.e("TAG", "Meaningful Message", e);
        } catch (IllegalAccessException e) {
            Log.e("TAG", "Meaningful Message", e);
        } catch (InstantiationException e) {
            Log.e("TAG", "Meaningful Message", e);
        } catch (InvocationTargetException e) {
            Log.e("TAG", "Meaningful Message", e);
        }
    }
}
