package com.eyinfo.generator;


import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.config.MergeConstants;

import java.util.List;

public class CustomBaseColumnListPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private String buildSql(IntrospectedTable introspectedTable) {
        StringBuilder sqlBuilder = new StringBuilder();
        List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();
        if (columnList != null) {
            int count = columnList.size();
            for (int i = 0; i < count; i++) {
                IntrospectedColumn column = columnList.get(i);
                String columnName = column.getActualColumnName();
                sqlBuilder.append("`").append(columnName).append("` ");
                if ((i + 1) < count) {
                    sqlBuilder.append(",");
                }
            }
        }
        return sqlBuilder.toString();
    }

    @Override
    public boolean sqlMapBaseColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("Base_Column_List");
        return false;
    }

    private void genBaseColumnListForXml(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("Base_Column_List");
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("sql");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "Base_Column_List"));
        element.addElement(new TextElement(buildSql(introspectedTable)));
        parentElement.addElement(element);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        genBaseColumnListForXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }
}
