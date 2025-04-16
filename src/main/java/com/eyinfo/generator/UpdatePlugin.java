package com.eyinfo.generator;

import com.eyinfo.generator.utils.PluginUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;

import java.util.List;
import java.util.Set;

public class UpdatePlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private void genUpdateBySelectiveForXml(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("updateBySelective");
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("update");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "updateBySelective"));
        element.addAttribute(new Attribute("parameterType", "java.util.Map"));
        StringBuilder sqlBuilder = new StringBuilder("    update");
        sqlBuilder.append(" ").append(tableName).append("\n");
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        Set<String> primaryFields = PluginUtils.getPrimaryFields(introspectedTable);
        if (columns != null) {
            int count = columns.size();
            sqlBuilder.append("            <set>\n");
            columns.forEach(m -> {
                String columnName = m.getActualColumnName();
                String jdbcTypeName = m.getJdbcTypeName();
                if (!primaryFields.contains(columnName) && !"id".equals(columnName)) {
                    String genFieldName = PluginUtils.genFieldName(columnName);
                    sqlBuilder.append("                <if test=\"et.").append(genFieldName).append(" != null\">\n");
                    sqlBuilder.append("                    `").append(columnName).append("`=#{et.").append(genFieldName).append(",jdbcType=").append(jdbcTypeName).append("},\n");
                    sqlBuilder.append("                </if>\n");
                }
            });
            sqlBuilder.append("            </set>\n");
        }
        sqlBuilder.append("        <where>\n");
        sqlBuilder.append("            <if test=\"ew != null and ew.sqlSegment != null\">\n");
        sqlBuilder.append("                ${ew.sqlSegment}\n");
        sqlBuilder.append("            </if>\n");
        sqlBuilder.append("        </where>");
        element.addElement(new TextElement(sqlBuilder.toString()));
        parentElement.addElement(element);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        genUpdateBySelectiveForXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }
}
