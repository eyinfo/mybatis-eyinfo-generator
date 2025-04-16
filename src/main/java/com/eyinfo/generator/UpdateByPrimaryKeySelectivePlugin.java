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

public class UpdateByPrimaryKeySelectivePlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        genUpdateByPrimaryKeySelectiveForXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    private void genUpdateByPrimaryKeySelectiveForXml(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("updateByPrimaryKeySelective");
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("update");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "updateByPrimaryKeySelective"));
        FullyQualifiedTable qualifiedTable = introspectedTable.getFullyQualifiedTable();
        String className = qualifiedTable.getDomainObjectName();
        JavaModelGeneratorConfiguration generatorConfiguration = context.getJavaModelGeneratorConfiguration();
        String targetPackage = generatorConfiguration.getTargetPackage();
        element.addAttribute(new Attribute("parameterType", String.format("%s.%s", targetPackage, className)));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("    update ").append(tableName).append("\n");
        sqlBuilder.append("    <set>\n");
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        int count = columns.size();
        columns.forEach(m -> {
            String columnName = m.getActualColumnName();
            String genFieldName = PluginUtils.genFieldName(columnName);
            String jdbcTypeName = m.getJdbcTypeName();
            sqlBuilder.append("      <if test=\"").append(genFieldName).append(" != null\">\n");
            sqlBuilder.append("        `").append(columnName).append("` = #{").append(genFieldName).append(",jdbcType=").append(jdbcTypeName).append("},\n");
            sqlBuilder.append("      </if>\n");
        });
        sqlBuilder.append("    </set>\n");
        sqlBuilder.append("    where id = #{id,jdbcType=BIGINT}");
        element.addElement(new TextElement(sqlBuilder.toString()));
        parentElement.addElement(element);
    }
}
