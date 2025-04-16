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
import java.util.concurrent.atomic.AtomicInteger;

public class BatchInsertPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private void genInsertItemsForXml(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("insertItems");
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("insert");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "insertItems"));
        element.addAttribute(new Attribute("parameterType", "java.util.List"));
        StringBuilder sqlBuilder = new StringBuilder("\n        insert into\n");
        sqlBuilder.append("        ").append(tableName).append("(\n");
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        Set<String> primaryFields = PluginUtils.getPrimaryFields(introspectedTable);
        if (columns != null) {
            int count = columns.size();
            AtomicInteger pos = new AtomicInteger(0);
            columns.forEach(m -> {
                String columnName = m.getActualColumnName();
                if (!primaryFields.contains(columnName) && !columnName.equals("id")) {
                    sqlBuilder.append("`").append(columnName).append("`");
                    if ((pos.get() + 1) < count) {
                        sqlBuilder.append(",");
                    }
                }
                pos.getAndIncrement();
            });
            sqlBuilder.append(")values\n");
            sqlBuilder.append("        <foreach collection=\"items\" item=\"item\" index=\"index\" separator=\",\">\n");
            sqlBuilder.append("            (\n");
            AtomicInteger vpos = new AtomicInteger(0);
            columns.forEach(m -> {
                String columnName = m.getActualColumnName();
                if (!primaryFields.contains(columnName) && !columnName.equals("id")) {
                    String genFieldName = PluginUtils.genFieldName(columnName);
                    sqlBuilder.append("            #{item.").append(genFieldName).append("}");
                    if ((vpos.get() + 1) < count) {
                        sqlBuilder.append(",\n");
                    } else {
                        sqlBuilder.append("\n");
                    }
                }
                vpos.getAndIncrement();
            });
            sqlBuilder.append("            )\n");
            sqlBuilder.append("        </foreach>");
        }
        element.addElement(new TextElement(sqlBuilder.toString()));
        parentElement.addElement(element);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        genInsertItemsForXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }
}
