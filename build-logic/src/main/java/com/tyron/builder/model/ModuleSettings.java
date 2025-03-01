package com.tyron.builder.model;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;

@SuppressWarnings({"unchecked", "ConstantConditions"})
public class ModuleSettings implements SharedPreferences {

    /**
     * Commonly used keys for project settings
     */
    public static final String LIBRARY_LIST = "libraries";
    public static final String SAVED_EDITOR_FILES = "editor_opened_files";
    public static final String USE_R8 = "useR8";
    public static final String TARGET_SDK_VERSION = "targetSdkVersion";
    public static final String MIN_SDK_VERSION = "minSdkVersion";
    public static final String VERSION_NAME = "versionName";
    public static final String VERSION_CODE = "versionCode";
    public static final String JAVA_TARGET_VERSION = "javaTargetVersion";
    public static final String JAVA_SOURCE_VERSION = "javaSourceVersion";
    public static final String MODULE_TYPE = "moduleType";

    private final File mConfigFile;
    private final Map<String, Object> mConfigMap;

    public ModuleSettings(File configFile) {
        mConfigFile = configFile;
        mConfigMap = parseFile();
    }

    private Map<String, Object> parseFile() {
        HashMap<String, Object> config = null;
        try {
            Gson gson = new GsonBuilder().create();
            config = gson.fromJson(new FileReader(mConfigFile),
                    new TypeToken<HashMap<String, Object>>(){}.getType());
        } catch (FileNotFoundException | JsonSyntaxException ignored) {

        }
        return config == null ? getDefaults() : config;
    }

    protected Map<String, Object> getDefaults() {
        Map<String, Object> map = new TreeMap<>();
        map.put(USE_R8, false);
        map.put(MIN_SDK_VERSION, 21);
        map.put(TARGET_SDK_VERSION, 30);
        map.put(VERSION_NAME, "1.0");
        map.put(VERSION_CODE, 1);
        map.put(JAVA_SOURCE_VERSION, "8");
        map.put(JAVA_TARGET_VERSION, "8");
        return map;
    }

    public void refresh() {
        mConfigMap.clear();
        mConfigMap.putAll(parseFile());
    }

    @Override
    public Map<String, ?> getAll() {
        return mConfigMap;
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String def) {
        return (String) mConfigMap.getOrDefault(key, def);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String s, @Nullable Set<String> set) {
        Object o = mConfigMap.get(s);
        if (o != null) {
            return new TreeSet<>(((ArrayList<String>) o));
        }
        return set;
    }

    @Override
    public int getInt(String s, int i) {
        Object o = mConfigMap.get(s);
        if (o == null) {
            return i;
        }
        String stringValue = String.valueOf(o);
        if (stringValue.contains(".")) {
            stringValue = stringValue.substring(0, stringValue.indexOf("."));
        }
        return Integer.parseInt(stringValue);
    }

    @Override
    public long getLong(String s, long l) {
        Object o = mConfigMap.get(s);
        if (o == null) {
            return l;
        }
        return Long.parseLong(String.valueOf(o));
    }

    @Override
    public float getFloat(String s, float f) {
        Object o = mConfigMap.get(s);
        if (o == null) {
            return f;
        }
        return Float.parseFloat(String.valueOf(o));
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return (boolean) mConfigMap.getOrDefault(s, b);
    }

    @Override
    public boolean contains(String s) {
        return mConfigMap.containsKey(s);
    }

    @Override
    public Editor edit() {
        return new Editor(mConfigFile, parseFile());
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    public static class Editor implements SharedPreferences.Editor {

        private final File mConfigFile;
        private final Map<String, Object> mValues;

        public Editor(File file, Map<String, Object> values) {
            mConfigFile = file;
            mValues = values;
        }

        @Override
        public SharedPreferences.Editor putString(String s, @Nullable String s1) {
            mValues.put(s, s1);
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String s, @Nullable Set<String> set) {
            mValues.put(s, set);
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String s, int i) {
            mValues.put(s, i);
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String s, long l) {
            mValues.put(s, l);
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String s, float v) {
            mValues.put(s, v);
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String s, boolean b) {
            mValues.put(s, b);
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String s) {
            mValues.remove(s);
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            mValues.clear();
            return this;
        }

        @Override
        public boolean commit() {
            String json;
            try {
                if (!mConfigFile.exists() && !mConfigFile.createNewFile()) {
                    return false;
                }
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                json = gson.toJson(mValues);
                FileUtils.writeStringToFile(mConfigFile, json, StandardCharsets.UTF_8);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void apply() {
            Executors.newSingleThreadExecutor().execute(this::commit);
        }
    }
}
