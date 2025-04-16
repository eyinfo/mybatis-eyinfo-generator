package com.eyinfo.generator.utils;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginUtils {
    public static String genFieldName(String columnName) {
        StringBuilder builder = new StringBuilder();
        String[] lst = columnName.split("_");
        for (int i = 0; i < lst.length; i++) {
            if (i == 0) {
                builder.append(lst[i].toLowerCase());
            } else {
                String attr = lst[i];
                if (attr.length() > 1) {
                    builder.append(attr.substring(0, 1).toUpperCase());
                    builder.append(attr.substring(1));
                } else {
                    builder.append(attr.toUpperCase());
                }
            }
        }
        return builder.toString();
    }

    public static Set<String> getPrimaryFields(IntrospectedTable introspectedTable) {
        Set<String> fields = new HashSet<>();
        List<IntrospectedColumn> columns = introspectedTable.getPrimaryKeyColumns();
        if (columns != null) {
            columns.forEach(m -> {
                fields.add(m.getActualColumnName());
            });
        }
        return fields;
    }
}
