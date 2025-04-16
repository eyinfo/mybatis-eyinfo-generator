package com.eyinfo.generator;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;

import java.util.List;

public class DeletePlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private void genDeletePlusForXml(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("deletePlus");
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("delete");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "deletePlus"));
        element.addAttribute(new Attribute("parameterType", "com.baomidou.mybatisplus.core.conditions.Wrapper"));
        StringBuilder sqlBuilder = new StringBuilder("    delete");
        sqlBuilder.append(" from ").append(tableName).append("\n");
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
        genDeletePlusForXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }
}
