package com.eyinfo.generator;

import com.eyinfo.generator.utils.PluginUtils;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.MergeConstants;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class InsertOrUpdatePlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("insertOrUpdate");
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("insert");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "insertOrUpdate"));
        FullyQualifiedTable qualifiedTable = introspectedTable.getFullyQualifiedTable();
        String className = qualifiedTable.getDomainObjectName();
        JavaModelGeneratorConfiguration generatorConfiguration = context.getJavaModelGeneratorConfiguration();
        String targetPackage = generatorConfiguration.getTargetPackage();
        element.addAttribute(new Attribute("parameterType", String.format("%s.%s", targetPackage, className)));
        Set<String> primaryFields = PluginUtils.getPrimaryFields(introspectedTable);
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        StringBuilder sqlBuilder = new StringBuilder("    INSERT INTO ");
        StringBuilder insertValues = new StringBuilder();
        StringBuilder updateValues = new StringBuilder();
        sqlBuilder.append(tableName).append("\n");
        sqlBuilder.append("        <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        insertValues.append("        <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
        if (columns != null) {
            int count = columns.size();
            AtomicInteger position = new AtomicInteger();
            columns.forEach(m -> {
                String columnName = m.getActualColumnName();
                String genFieldName = PluginUtils.genFieldName(columnName);
                if (!primaryFields.contains(columnName)) {
                    sqlBuilder.append(String.format("            <if test=\"%s != null\">", genFieldName));
                    sqlBuilder.append("`").append(columnName).append("`");
                } else {
                    sqlBuilder.append("            `").append(columnName).append("`");
                }
                if (!primaryFields.contains(columnName)) {
                    insertValues.append(String.format("            <if test=\"%s != null\">", genFieldName));
                    insertValues.append("#{").append(genFieldName).append("}");
                } else {
                    insertValues.append("            #{").append(genFieldName).append("}");
                }
                if (!primaryFields.contains(columnName)) {
                    updateValues.append(String.format("        <if test=\"%s != null\">", genFieldName));
                    updateValues.append("`").append(columnName).append("`=#{").append(genFieldName).append("}");
                    int nextPost = position.intValue() + 1;
                    if (nextPost < count) {
                        IntrospectedColumn next = columns.get(nextPost);
                        String nextActualColumnName = next.getActualColumnName();
                        String nextGenFieldName = PluginUtils.genFieldName(nextActualColumnName);
                        updateValues.append(String.format("<if test=\"%s != null\">,", nextGenFieldName)).append("</if>");
                    }
                    updateValues.append("</if>\n");
                }
                if ((position.intValue() + 1) < count) {
                    sqlBuilder.append(",");
                    insertValues.append(",");
                    if (primaryFields.contains(columnName)) {
                        sqlBuilder.append("\n");
                        insertValues.append("\n");
                    }
                }
                if (!primaryFields.contains(columnName)) {
                    sqlBuilder.append("</if>\n");
                    insertValues.append("</if>\n");
                }
                position.getAndIncrement();
            });
        }
        sqlBuilder.append("        </trim>\n");
        insertValues.append("        </trim>\n");
        sqlBuilder.append(insertValues.toString()).append("        ON DUPLICATE KEY\n");
        sqlBuilder.append("        UPDATE \n").append(updateValues.toString());
        element.addElement(new TextElement(sqlBuilder.toString()));
        parentElement.addElement(element);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    private boolean hasAttribute(XmlElement element, String attrName) {
        boolean flag = false;
        List<Attribute> attributes = element.getAttributes();
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            if (attrName.equals(attribute.getValue())) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
