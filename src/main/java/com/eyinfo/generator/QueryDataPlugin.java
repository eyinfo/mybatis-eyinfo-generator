package com.eyinfo.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;

import java.util.List;

public class QueryDataPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private void genDataPlusForXml(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("getDataPlus");
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("select");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "getDataPlus"));
        List<IntrospectedColumn> blobColumns = introspectedTable.getBLOBColumns();
        if (blobColumns != null && blobColumns.size() > 0) {
            element.addAttribute(new Attribute("resultMap", "ResultMapWithBLOBs"));
        } else {
            element.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        }
        element.addAttribute(new Attribute("parameterType", "com.baomidou.mybatisplus.core.conditions.Wrapper"));
        StringBuilder sqlBuilder = new StringBuilder("    select\n");
        sqlBuilder.append("        <choose>\n");
        sqlBuilder.append("            <when test=\"ew != null and ew.sqlSelect != null and ew.sqlSelect != ''\">\n");
        sqlBuilder.append("                ${ew.sqlSelect}\n");
        sqlBuilder.append("            </when>\n");
        sqlBuilder.append("            <otherwise>\n");
        sqlBuilder.append("                <include refid=\"Base_Column_List\"/>\n");
        if (blobColumns != null && blobColumns.size() > 0) {
            sqlBuilder.append(",");
            sqlBuilder.append("                <include refid=\"Blob_Column_List\" />");
        }
        sqlBuilder.append("            </otherwise>\n");
        sqlBuilder.append("        </choose>\n");
        sqlBuilder.append("\n        from ").append(tableName).append("\n");
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
        genDataPlusForXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }
}
