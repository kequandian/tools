package com.intellij.plugin.json.util;

/**
 * Created by vincent on 2016/5/4.
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WriterUtil {
    protected PsiClass mClass;
    private com.intellij.psi.PsiElementFactory mFactory;
    private String jsonStr;
    private List<String> keyWordList = new ArrayList();

    public WriterUtil(String jsonStr, Project project, PsiClass mClass) {
        this.mFactory = JavaPsiFacade.getElementFactory(project);
        this.jsonStr = jsonStr;
        this.mClass = mClass;
    }

    public void generate() {
        JsonObject jsonObject = null;

        try {
            JsonParser parser = new JsonParser();
            jsonObject = (JsonObject) parser.parse(this.jsonStr);

        } catch (Exception var8) {
            String temp = this.jsonStr.replaceAll("/\\*\\*[\\S\\s]*?\\*/", "");
            String jsonTS = temp.replaceAll("//[^\"\\]\\}\']*\\s+", "");

            try {
                jsonObject = (JsonObject) new JsonParser().parse(jsonTS);
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }

        if (jsonObject != null) {
            try {
                this.parseJson(jsonObject);

            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }
    }

    public void parseJson(JsonObject json) {
        Iterator<Map.Entry<String, com.google.gson.JsonElement>> iter = json.entrySet().iterator();
        ArrayList list = new ArrayList();
        for (; iter.hasNext(); ) {
            list.add(iter.next().getKey());
        }

        List fields = this.createField(json, list, this.mClass);

        this.createSetMethod(json, fields, list, this.mClass);
        this.createGetMethod(json, fields, list, this.mClass);
    }

    private List<String> createField(JsonObject json, List<String> list, PsiClass mClass) {
        ArrayList fields = new ArrayList();

        String key;
        for (int i = 0; i < list.size(); ++i) {
            key = list.get(i);
            Object type = json.get(key);
            StringBuilder fieldBuilder = new StringBuilder();

            if (key.contains("_")) {
                fieldBuilder.append("@com.google.gson.annotations.SerializedName(\"" + key + "\")\n");

                String[] arr = key.split("_");
                if (arr.length > 1) {
                    StringBuilder builder = new StringBuilder(arr[0]);
                    for (int n = 1; n < arr.length; n++) {
                        builder.append(StringUtil.capitalize(arr[n]));
                    }

                    key = builder.toString();
                }
            }

            fields.add(key);

            String typeString = this.typeByValue(mClass, key, type, true);
            fieldBuilder.append("private ").append(typeString).append(key).append(" ; \n");

            // append field
            String filedString = fieldBuilder.toString();
            mClass.add(this.mFactory.createFieldFromText(filedString, mClass));
        }

        return fields;
    }


    @NotNull
    private String typeByValue(PsiClass mClass, String key, Object type) {
        return this.typeByValue(mClass, key, type, false);
    }

    @NotNull
    private String typeByValue(PsiClass mClass, String key, Object type, boolean createClassSub) {
        String typeStr;
        if (type instanceof Boolean) {
            typeStr = " boolean ";
        } else if (type instanceof Integer) {
            if (key.compareTo("id") == 0 ||
                    key.endsWith("Id")) {
                typeStr = " long ";
            } else {
                typeStr = " int ";
            }
        } else if (type instanceof Double) {
            typeStr = " double ";
        } else if (type instanceof Long) {
            typeStr = " long ";
        } else if (type instanceof String) {
            typeStr = " String ";
        } else if (type instanceof Character) {
            typeStr = " char ";
        } else if (type instanceof JsonObject) {
            typeStr = " " + this.createClassSubName(mClass, key, type, mClass, createClassSub) + " ";
            if (createClassSub) {
                this.createClassSub(typeStr, type, mClass);
            }
        } else if (type instanceof JsonArray) {
            typeStr = " java.util.List<" + this.createClassSubName(mClass, key, type, mClass, createClassSub) + "> ";
        } else {
            typeStr = " String ";
        }

        return typeStr;
    }

    private void createClassSub(String className, Object o, PsiClass mClass) {
        if (o instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) o;
            this.createClassSub(className, jsonObject, mClass);
        }

    }

    private void createClassSub(String className, JsonObject json, PsiClass mClass) {
        String classContent = "/** */\n public  " + className + "(){" + "}";
        PsiClass subClass = this.mFactory.createClass(className.trim());
        subClass.setName(className.trim());

        Iterator<Map.Entry<String, com.google.gson.JsonElement>> iter = json.entrySet().iterator();
        ArrayList list = new ArrayList();
        for (; iter.hasNext(); ) {
            list.add(iter.next().getKey());
        }

        List fields = this.createField(json, list, subClass);
        this.createSetMethod(json, fields, list, subClass);
        this.createGetMethod(json, fields, list, subClass);

        mClass.add(subClass);
    }

    private String createClassSubName(PsiClass aClass, String key, Object o, PsiClass mClass, boolean createClassSUb) {
        String name = "";
        if (o instanceof JsonObject) {
            name = key.substring(0, 1).toUpperCase() + key.substring(1) + "Entity";
        } else if (o instanceof JsonArray) {
            JsonArray jsonArray = (JsonArray) o;
            if (jsonArray.size() > 0) {
                Object item = jsonArray.get(0);
                name = this.typeByValue(mClass, key, item, createClassSUb);
            } else {
                name = "?";
            }
        }

        return name;
    }

    private void createSetMethod(JsonObject json, List<String> fields, List<String> keys, PsiClass mClass) {
        for (int i = 0; i < keys.size(); ++i) {
            String key = (String) keys.get(i);
            String field = (String) fields.get(i);
            Object type = json.get(key);
            String typeStr = this.typeByValue(mClass, field, type);
            String method = "public void set" + StringUtil.capitalize(field) + "( " + typeStr + " " + field + ") {   this." + field + " = " + field + ";} ";
            mClass.add(this.mFactory.createMethodFromText(method, mClass));
        }
    }

    private void createGetMethod(JsonObject json, List<String> fields, List<String> keys, PsiClass mClass) {
        for (int i = 0; i < keys.size(); ++i) {
            String key = (String) keys.get(i);
            String field = (String) fields.get(i);
            Object type = json.get(key);
            String typeStr = this.typeByValue(mClass, field, type);
            String method;
            if (type instanceof Boolean) {
                method = "public " + typeStr + " is" + StringUtil.capitalize(field) + "() {   return " + field + " ;} ";
                mClass.add(this.mFactory.createMethodFromText(method, mClass));
            } else {
                method = "public " + typeStr + " get" + StringUtil.capitalize(field) + "() {   return " + field + " ;} ";
                mClass.add(this.mFactory.createMethodFromText(method, mClass));
            }
        }
    }

    private void createKeyWords() {
        this.keyWordList.add("default");
        this.keyWordList.add("public");
        this.keyWordList.add("abstract");
        this.keyWordList.add("null");
        this.keyWordList.add("final");
        this.keyWordList.add("void");
        this.keyWordList.add("implements");
        this.keyWordList.add("this");
        this.keyWordList.add("instanceof");
        this.keyWordList.add("native");
        this.keyWordList.add("new");
        this.keyWordList.add("goto");
        this.keyWordList.add("const");
        this.keyWordList.add("volatile");
        this.keyWordList.add("return");
        this.keyWordList.add("finally");
    }

    private String buildComment(JsonObject json, List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("/** \n");

        int i;
        String key;
        for (i = 0; i < list.size(); ++i) {
            key = (String) list.get(i);
            sb.append("* " + key + " : " + json.get(key) + "\n");
        }
        sb.append("*/ \n");

        return sb.toString();
    }

    public boolean checkKeyWord(String key) {
        return this.keyWordList.contains(key);
    }
}
