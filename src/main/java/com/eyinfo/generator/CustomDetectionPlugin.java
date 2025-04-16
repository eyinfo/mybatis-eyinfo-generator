package com.eyinfo.generator;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomDetectionPlugin extends PluginAdapter {

    private Map<String, String> fieldMap = new HashMap<>();
    private Set<String> excludFields = new HashSet<String>();
    private Set<String> excludAttrs = new HashSet<>();
    private Set<String> methods = new HashSet<>();

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        fieldMap.clear();
        excludFields.clear();
        initExclud();
    }

    private void initExclud() {
        excludFields.add("id");
        excludFields.add("createTime");
        excludFields.add("createBy");
        excludFields.add("updateTime");
        excludFields.add("modifyTime");
        excludFields.add("updateBy");
        excludFields.add("isDeleted");
        for (String field : excludFields) {
            String getterMethod = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
            String setterMethod = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
            excludAttrs.add(getterMethod);
            excludAttrs.add(setterMethod);
        }
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private boolean hasAnnotation(List<String> annotations) {
        if (annotations == null) {
            return false;
        }
        boolean flag = false;
        for (String annotation : annotations) {
            if (annotation.contains("com.baomidou.mybatisplus.annotation.TableName")) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private String getRootPath() {
        String rootPath = System.getProperty("user.dir");
        if (rootPath == null || rootPath.isEmpty()) {
            File file = new File("");
            String absolutePath = file.getAbsolutePath();
            return absolutePath;
        }
        return rootPath;
    }

    private File getGeneratedFile(String rootPath, String targetProject, String relativePath) {
        File root = new File(rootPath);
        File project = new File(root, targetProject);
        File target = new File(project, relativePath);
        return target;
    }

//    /**
//     * 检验mapper对象是否已经生成过
//     * 1. 实体文件、mapper文件、xml文件
//     * 2. 实体字段信息；
//     * 3. mapper方法名校验，略：已提取到基类中；
//     * 4. xml方法名校验;
//     */
//    @Override
//    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
//        Set<String> actualColumnNames = getActualColumnNames(introspectedTable);
//        boolean generatedFilesStatus = checkGeneratedFiles(sqlMap, introspectedTable);
//        if (!generatedFilesStatus) {
//            return true;
//        }
//        boolean checkEntityFields = checkEntityFields(sqlMap.getTargetProject(), introspectedTable, actualColumnNames);
//        if (!checkEntityFields) {
//            return true;
//        }
//        boolean checkMapperXmlMethods = checkMapperXmlMethods(sqlMap, introspectedTable);
//        if (!checkMapperXmlMethods) {
//            return true;
//        }
//        return false;
//    }

    private Set<String> getMapperXmlFileMethodNames(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        Set<String> methodNames = new HashSet<>();
        String targetProject = sqlMap.getTargetProject();
        String sqlMapNamespace = introspectedTable.getMyBatis3FallbackSqlMapNamespace();
        String relativePath = sqlMapNamespace.replace(".", "/");
        File generatedFile = getGeneratedFile(getRootPath(), targetProject, String.format("%s.xml", relativePath));
        try {
            String content = new String(Files.readAllBytes(Paths.get(generatedFile.getAbsolutePath())));
            Pattern pattern = Pattern.compile("<\\w+ id=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                methodNames.add(matcher.group(1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return methodNames;
    }

    private Set<String> getActualMethodNames() {
        Set<String> methodNames = new HashSet<>();
        methodNames.add("BaseResultMap");
        methodNames.add("Base_Column_List");
        methodNames.add("selectByPrimaryKey");
        methodNames.add("deleteByPrimaryKey");
        methodNames.add("updateByPrimaryKeySelective");
        methodNames.add("insertSelective");
        methodNames.add("getDataPlus");
        methodNames.add("getListPlus");
        methodNames.add("countPlus");
        methodNames.add("deletePlus");
        methodNames.add("updateItems");
        methodNames.add("insertItems");
        methodNames.add("updateBySelective");
        return methodNames;
    }

    private boolean checkMapperXmlMethods(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        Set<String> xmlFileMethodNames = getMapperXmlFileMethodNames(sqlMap, introspectedTable);
        Set<String> actualMethodNames = getActualMethodNames();
        for (String actualMethodName : actualMethodNames) {
            if (!xmlFileMethodNames.contains(actualMethodName)) {
                return false;
            }
        }
        return true;
    }

    private Set<String> parseFieldNames(String content) {
        Set<String> fieldNames = new HashSet<>();
        Pattern pattern = Pattern.compile("public static String (\\w+)\\s*=\\s*\\\"(.*?)\\\";");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String fieldName = matcher.group(2);
            fieldNames.add(fieldName);
        }
        return fieldNames;
    }

    private Set<String> getActualColumnNames(IntrospectedTable introspectedTable) {
        Set<String> columnNames = new HashSet<>();
        List<IntrospectedColumn> allColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn column : allColumns) {
            columnNames.add(column.getActualColumnName());
        }
        return columnNames;
    }

    private boolean checkEntityFields(String targetProject, IntrospectedTable introspectedTable, Set<String> actualColumnNames) {
        String recordType = introspectedTable.getBaseRecordType();
        String relativePath = recordType.replace(".", "/");
        File generatedFile = getGeneratedFile(getRootPath(), targetProject, String.format("%s.java", relativePath));
        try {
            String content = new String(Files.readAllBytes(Paths.get(generatedFile.getAbsolutePath())));
            Set<String> fieldNames = parseFieldNames(content);
            //比较字段数量、字段名
            if (fieldNames.size() != actualColumnNames.size()) {
                return false;
            }
            for (String columnName : actualColumnNames) {
                if (!fieldNames.contains(columnName)) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean checkGeneratedFiles(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        boolean checkEntity = checkEntity(introspectedTable, sqlMap.getTargetProject());
        boolean checkMapperXml = checkMapperXml(introspectedTable, sqlMap.getTargetProject());
        return checkEntity && checkMapperXml;
    }

    private boolean checkMapperXml(IntrospectedTable introspectedTable, String targetProject) {
        String sqlMapNamespace = introspectedTable.getMyBatis3FallbackSqlMapNamespace();
        String relativePath = sqlMapNamespace.replace(".", "/");
        File generatedFile = getGeneratedFile(getRootPath(), targetProject, String.format("%s.xml", relativePath));
        return generatedFile.exists();
    }

    private boolean checkEntity(IntrospectedTable introspectedTable, String targetProject) {
        String recordType = introspectedTable.getBaseRecordType();
        String relativePath = recordType.replace(".", "/");
        File generatedFile = getGeneratedFile(getRootPath(), targetProject, String.format("%s.java", relativePath));
        return generatedFile.exists();
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        fieldMap.clear();
        return super.sqlMapGenerated(sqlMap, introspectedTable);
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String actualColumnName = introspectedColumn.getActualColumnName();
        fieldMap.put(field.getName(), actualColumnName);
        if (!excludFields(field, topLevelClass)) {
            return false;
        }
        if (!hasAnnotation(topLevelClass.getAnnotations())) {
            String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
            topLevelClass.addAnnotation(String.format("@com.baomidou.mybatisplus.annotation.TableName(\"%s\")", tableName));
        }
        if (introspectedColumn.isIdentity()) {
            field.addAnnotation(String.format("@com.baomidou.mybatisplus.annotation.TableId(\"%s\")", actualColumnName));
        } else {
            field.addAnnotation(String.format("@com.baomidou.mybatisplus.annotation.TableField(\"%s\")", actualColumnName));
        }
        return true;
    }

    private boolean remoteAttrMethod(Method method, TopLevelClass topLevelClass) {
        List<Method> methods = topLevelClass.getMethods();
        if (methods != null) {
            if (excludAttrs.contains(method.getName())) {
                methods.remove(method);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return remoteAttrMethod(method, topLevelClass);
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return remoteAttrMethod(method, topLevelClass);
    }

    private boolean excludFields(Field field, TopLevelClass topLevelClass) {
        if (excludFields.contains(field.getName())) {
            topLevelClass.getFields().remove(field);
            return false;
        }
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
//        String targetProject = "/src/main/java";
//        boolean checkEntity = checkEntity(introspectedTable, targetProject);
//        if (!checkEntity) {
//            return true;
//        }
//        Set<String> actualColumnNames = getActualColumnNames(introspectedTable);
//        boolean checkEntityFields = checkEntityFields(targetProject, introspectedTable, actualColumnNames);
//        if (checkEntityFields) {
//            return false;
//        }
        if (fieldMap.size() > 0) {
            for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                Field customField = new Field(String.format("%sDbName", entry.getKey()), new FullyQualifiedJavaType("java.lang.String"));
                customField.setVisibility(JavaVisibility.PUBLIC);
                customField.setStatic(true);
                customField.setInitializationString(String.format("\"`%s`\"", entry.getValue()));
                topLevelClass.addField(customField);
            }
        }
        generateObtain(topLevelClass, introspectedTable);
        return true;
    }

    private void generateObtain(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<Method> methods = topLevelClass.getMethods();
        if (!methods.contains("obtain")) {
            Method method = new Method("obtain");
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setStatic(true);
            FullyQualifiedTable qualifiedTable = introspectedTable.getFullyQualifiedTable();
            String className = qualifiedTable.getDomainObjectName();
            method.setReturnType(new FullyQualifiedJavaType(className));
            method.addBodyLine("return new " + className + "();");
            topLevelClass.addMethod(method);
            fieldMap.clear();
        }
    }
}
