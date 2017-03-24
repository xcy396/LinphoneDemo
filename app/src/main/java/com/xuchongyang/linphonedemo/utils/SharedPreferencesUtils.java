package com.xuchongyang.linphonedemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

/**
 * Created by xu on 16/10/30.
 */

public class SharedPreferencesUtils {

    /**
     * 保存数据到指定文件中
     *
     * @param context
     *            上下文
     * @param sharedPreferencesFileName
     *            用SharedPreferences保存的文件名
     * @param key
     *            键值
     * @param value
     *            键对应的值
     */
    public static void save(Context context, String sharedPreferencesFileName, String key, Object value) {
        SharedPreferences sp = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        save(sp, key, value);
    }

    /**
     * 保存数据到默认的文件中
     *
     * @param context
     *            上下文
     * @param key
     *            键值
     * @param value
     *            键对应的值
     */
    public static void save(Context context, String key, Object value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        save(sp, key, value);
    }

    private static void save(SharedPreferences sp, String key, Object value) {
        if (value == null) {
            return;
        }
        String type = value.getClass().getSimpleName();
        SharedPreferences.Editor editor = sp.edit();
        if ("String".equals(type)) {
            editor.putString(key, (String) value);
        } else if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) value);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) value);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) value);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) value);
        }

        editor.apply();
    }

    /**
     * 根据KEY和文件名，取得保存的值，否则返回默认值
     *
     * @param context
     *            上下文
     * @param sharedPreferencesFileName
     *            用SharedPreferences保存的文件名
     * @param key
     *            键值
     * @param defaultValue
     *            默认值
     * @return 返回键对应的值
     */
    public static Object get(Context context, String sharedPreferencesFileName, String key, Object defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        return get(sp, key, defaultValue);
    }

    /**
     * 根据KEY，到默认的文件中取得保存的值，否则返回默认值
     *
     * @param context
     *            上下文
     * @param key
     *            键值
     * @param defaultValue
     *            键对应的值
     * @return
     */
    public static Object get(Context context, String key, Object defaultValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return get(sp, key, defaultValue);
    }

    private static Object get(SharedPreferences sp, String key, Object defaultValue) {
        String type = defaultValue.getClass().getSimpleName();

        if ("String".equals(type)) {
            return sp.getString(key, (String) defaultValue);
        } else if ("Integer".equals(type)) {
            return sp.getInt(key, (Integer) defaultValue);
        } else if ("Boolean".equals(type)) {
            return sp.getBoolean(key, (Boolean) defaultValue);
        } else if ("Float".equals(type)) {
            return sp.getFloat(key, (Float) defaultValue);
        } else if ("Long".equals(type)) {
            return sp.getLong(key, (Long) defaultValue);
        }

        return null;
    }

    /**
     * 根据key值删除
     *
     * @param context
     *            Context上下文
     * @param sharedPreferencesFileName
     *            用SharedPreferences保存的文件名
     * @param key
     *            key值
     */
    public static void remove(Context context, String sharedPreferencesFileName, String key) {
        SharedPreferences sp = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * 根据key值删除
     *
     * @param context
     *            Context上下文
     * @param key
     *            key值
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * 获取sharedpreferences中所有数据
     *
     * @param context
     *            上下文
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getAll();
    }

    /**
     * 获取sharedpreferences中所有数据
     *
     * @param context
     *            上下文
     * @param sharedPreferencesFileName
     *            用SharedPreferences保存的文件名
     */
    public static Map<String, ?> getAll(Context context, String sharedPreferencesFileName) {
        SharedPreferences sp = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        return sp.getAll();
    }
}
