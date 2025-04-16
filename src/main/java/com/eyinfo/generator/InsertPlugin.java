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

public class InsertPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        genInsertSelectiveForXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    private void genInsertSelectiveForXml(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("insertSelective");
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("insert");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "insertSelective"));
        FullyQualifiedTable qualifiedTable = introspectedTable.getFullyQualifiedTable();
        String className = qualifiedTable.getDomainObjectName();
        JavaModelGeneratorConfiguration generatorConfiguration = context.getJavaModelGeneratorConfiguration();
        String targetPackage = generatorConfiguration.getTargetPackage();
        element.addAttribute(new Attribute("parameterType", String.format("%s.%s", targetPackage, className)));
        element.addAttribute(new Attribute("useGeneratedKeys", "true"));
        element.addAttribute(new Attribute("keyProperty", "id"));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("    insert into ").append(tableName).append("\n");
        sqlBuilder.append("    <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        Set<String> primaryFields = PluginUtils.getPrimaryFields(introspectedTable);
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        int count = columns.size();
        columns.forEach(m -> {
            String columnName = m.getActualColumnName();
            String genFieldName = PluginUtils.genFieldName(columnName);
            if (!primaryFields.contains(columnName) && !columnName.equals("id")) {
                sqlBuilder.append("      <if test=\"").append(genFieldName).append(" != null\">\n");
                sqlBuilder.append("        `").append(columnName).append("`,\n");
                sqlBuilder.append("      </if>\n");
            }
        });
        sqlBuilder.append("    </trim>\n");
        sqlBuilder.append("    <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
        columns.forEach(m -> {
            String columnName = m.getActualColumnName();
            String jdbcTypeName = m.getJdbcTypeName();
            String genFieldName = PluginUtils.genFieldName(columnName);
            if (!primaryFields.contains(columnName) && !columnName.equals("id")) {
                sqlBuilder.append("      <if test=\"").append(genFieldName).append(" != null\">\n");
                sqlBuilder.append("        #{").append(genFieldName).append(",jdbcType=").append(jdbcTypeName).append("},\n");
                sqlBuilder.append("      </if>\n");
            }
        });
        sqlBuilder.append("    </trim>\n");
        element.addElement(new TextElement(sqlBuilder.toString()));
        parentElement.addElement(element);
    }
}
