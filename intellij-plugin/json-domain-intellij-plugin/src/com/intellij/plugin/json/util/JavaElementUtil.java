package com.intellij.plugin.json.util;

/**
 * Created by vincent on 2016/5/4.
 */

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;

public class JavaElementUtil {
    private PsiElementFactory mFactory;
    private PsiClass mClass;

    public JavaElementUtil(Project project, PsiClass mClass) {
        this.mFactory = JavaPsiFacade.getElementFactory(project);
        this.mClass = mClass;
    }

    public JavaElementUtil(PsiElementFactory factory, PsiClass mClass) {
        this.mFactory = factory;
        this.mClass = mClass;
    }

    private void createField(String field, String fieldType) {

        String fieldText = null;
        {
            StringBuilder fieldBuilder = new StringBuilder();

            if (field.contains("_")) {
                fieldBuilder.append("@com.google.gson.annotations.SerializedName(\"" + field + "\")\n");

                String[] arr = field.split("_");
                if (arr.length > 1) {
                    StringBuilder builder = new StringBuilder(arr[0]);
                    for (int n = 1; n < arr.length; n++) {
                        builder.append(StringUtil.capitalize(arr[n]));
                    }

                    field = builder.toString();
                }
            }

            fieldBuilder.append("private ").append(fieldType).append(field).append(";");

            fieldText = fieldBuilder.toString();
        }

        mClass.add(this.mFactory.createFieldFromText(fieldText, mClass));
    }

    private void createSetMethod(String field, String fieldType) {
        String method = "public void set" + StringUtil.capitalize(field) + "( " + fieldType + " " + field + ") {   this." + field + " = " + field + ";} ";
        mClass.add(this.mFactory.createMethodFromText(method, mClass));
    }

    private void createGetMethod(String field, String fieldType) {
        String method = "public " + fieldType + " get" + StringUtil.capitalize(field) + "() {   return " + field + " ;} ";
        mClass.add(this.mFactory.createMethodFromText(method, mClass));
    }
}
