package com.intellij.plugin.util;

/**
 * Created by vincent on 2016/5/4.
 */
import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.AppearanceOptionsTopHitProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WriterUtil{
    protected PsiClass mClass;
    private PsiElementFactory mFactory;
    private String jsonStr;
    private List<String> keyWordList = new ArrayList();

    public WriterUtil(String jsonStr, Project project, PsiClass mClass) {
        this.mFactory = JavaPsiFacade.getElementFactory(project);
        this.jsonStr = jsonStr;
        this.mClass = mClass;
    }

    public void generate() {
        JSONObject json = null;

        try {
            json = new JSONObject(this.jsonStr);
        } catch (Exception var8) {
            String temp = this.jsonStr.replaceAll("/\\*\\*[\\S\\s]*?\\*/", "");
            String jsonTS = temp.replaceAll("//[^\"\\]\\}\']*\\s+", "");

            try {
                json = new JSONObject(jsonTS);
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }

        if(json != null) {
            try {
                this.parseJson(json);

            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }
    }

    public void parseJson(JSONObject json) {
        Iterator<String> iter = json.keys();
        ArrayList list = new ArrayList();
        for (;iter.hasNext();){
            list.add(iter.next());
        }

        List fields = this.createField(json, list, this.mClass);

        this.createSetMethod(json, fields, list, this.mClass);
        this.createGetMethod(json, fields, list, this.mClass);
    }

    private List<String> createField(JSONObject json, List<String> list, PsiClass mClass) {
        ArrayList fields = new ArrayList();

        try {
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
        }catch (JSONException e){
        }

        return fields;
    }

    private void createKeyWords(){
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

    private String buildComment(JSONObject json, List<String> list){
        try {
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
        }catch (JSONException e){
        }

        return null;
    }


    public boolean checkKeyWord(String key) {
        return this.keyWordList.contains(key);
    }

    @NotNull
    private String typeByValue(PsiClass mClass, String key, Object type) {
        return this.typeByValue(mClass, key, type, false);
    }

    @NotNull
    private String typeByValue(PsiClass mClass, String key, Object type, boolean createClassSub) {
        String typeStr;
        if(type instanceof Boolean) {
            typeStr = " boolean ";
        } else if(type instanceof Integer) {
            if(key.compareTo("id")==0 ||
                    key.endsWith("Id")){
                typeStr = " long ";
            }else {
                typeStr = " int ";
            }
        } else if(type instanceof Double) {
            typeStr = " double ";
        } else if(type instanceof Long) {
            typeStr = " long ";
        } else if(type instanceof String) {
            typeStr = " String ";
        } else if(type instanceof Character) {
            typeStr = " char ";
        } else if(type instanceof JSONObject) {
            typeStr = " " + this.createClassSubName(mClass, key, type, mClass, createClassSub) + " ";
            if(createClassSub) {
                this.createClassSub(typeStr, type, mClass);
            }
        } else if(type instanceof JSONArray) {
            typeStr = " java.util.List<" + this.createClassSubName(mClass, key, type, mClass, createClassSub) + "> ";
        } else {
            typeStr = " String ";
        }

        return typeStr;
    }

    private void createClassSub(String className, Object o, PsiClass mClass) {
        if(o instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject)o;
            this.createClassSub(className, jsonObject, mClass);
        }

    }

    private void createClassSub(String className, JSONObject json, PsiClass mClass) {
        String classContent = "/** */\n public  " + className + "(){" + "}";
        PsiClass subClass = this.mFactory.createClass(className.trim());
        subClass.setName(className.trim());
        Iterator<String> iter = json.keys();
        ArrayList list = new ArrayList();
        for(;iter.hasNext();){
            list.add(iter.next());
        }

        List fields = this.createField(json, list, subClass);
        this.createSetMethod(json, fields, list, subClass);
        this.createGetMethod(json, fields, list, subClass);

        mClass.add(subClass);
    }

    private String createClassSubName(PsiClass aClass, String key, Object o, PsiClass mClass, boolean createClassSUb) {
        String name = "";
        try {
            if (o instanceof JSONObject) {
                name = key.substring(0, 1).toUpperCase() + key.substring(1) + "Entity";
            } else if (o instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) o;
                if (jsonArray.length() > 0) {
                    Object item = jsonArray.get(0);
                    name = this.typeByValue(mClass, key, item, createClassSUb);
                } else {
                    name = "?";
                }
            }
        }catch (JSONException e){

        }

        return name;
    }

    private void createSetMethod(JSONObject json, List<String> fields, List<String> keys, PsiClass mClass) {
        try {

            for (int i = 0; i < keys.size(); ++i) {
                String key = (String) keys.get(i);
                String field = (String) fields.get(i);
                Object type = json.get(key);
                String typeStr = this.typeByValue(mClass, field, type);
                String method = "public void set" + this.captureName(field) + "( " + typeStr + " " + field + ") {   this." + field + " = " + field + ";} ";
                mClass.add(this.mFactory.createMethodFromText(method, mClass));
            }
        }catch (JSONException e){

        }
    }

    public String captureName(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    private void createGetMethod(JSONObject json, List<String> fields, List<String> keys, PsiClass mClass) {
        try {
            for (int i = 0; i < keys.size(); ++i) {
                String key = (String) keys.get(i);
                String field = (String) fields.get(i);
                Object type = json.get(key);
                String typeStr = this.typeByValue(mClass, field, type);
                String method;
                if (type instanceof Boolean) {
                    method = "public " + typeStr + " is" + this.captureName(field) + "() {   return " + field + " ;} ";
                    mClass.add(this.mFactory.createMethodFromText(method, mClass));
                } else {
                    method = "public " + typeStr + " get" + this.captureName(field) + "() {   return " + field + " ;} ";
                    mClass.add(this.mFactory.createMethodFromText(method, mClass));
                }
            }
        }catch (JSONException e){

        }

    }
}
