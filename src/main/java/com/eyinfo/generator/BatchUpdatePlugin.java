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

public class BatchUpdatePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private void genUpdateItemsForXml(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("updateItems");
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("update");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "updateItems"));
        element.addAttribute(new Attribute("parameterType", "java.util.List"));
        StringBuilder sqlBuilder = new StringBuilder("\n        <foreach collection=\"items\" index=\"index\" item=\"item\">\n");
        sqlBuilder.append("            update ").append(tableName).append("\n");
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        Set<String> primaryFields = PluginUtils.getPrimaryFields(introspectedTable);
        if (columns != null) {
            int count = columns.size();
            sqlBuilder.append("            <set>\n");
            columns.forEach(m -> {
                String columnName = m.getActualColumnName();
                if (!primaryFields.contains(columnName) && !columnName.equals("id")) {
                    String genFieldName = PluginUtils.genFieldName(columnName);
                    sqlBuilder.append("                <if test=\"item.").append(genFieldName).append(" != null\">\n");
                    sqlBuilder.append("                    `").append(columnName).append("`=#{item.").append(genFieldName).append("},\n");
                    sqlBuilder.append("                </if>\n");
                }
            });
            sqlBuilder.append("            </set>\n");
            sqlBuilder.append("            where id=#{item.id};\n");
            sqlBuilder.append("        </foreach>");
        }
        element.addElement(new TextElement(sqlBuilder.toString()));
        parentElement.addElement(element);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        genUpdateItemsForXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }
}
