package com.eyinfo.generator;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.MergeConstants;

import java.util.List;

public class DeleteByPrimaryKeyPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        genDeleteByPrimaryKeyForXml(document, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    private void genDeleteByPrimaryKeyForXml(Document document, IntrospectedTable introspectedTable) {
        introspectedTable.removeAttribute("deleteByPrimaryKey");
        String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement element = new XmlElement("delete");
        element.addElement(new TextElement("    <!-- ELEMENT FOR GENERATOR MERGE - " + MergeConstants.NEW_ELEMENT_TAG + " -->"));
        element.addAttribute(new Attribute("id", "deleteByPrimaryKey"));
        FullyQualifiedTable qualifiedTable = introspectedTable.getFullyQualifiedTable();
        String className = qualifiedTable.getDomainObjectName();
        JavaModelGeneratorConfiguration generatorConfiguration = context.getJavaModelGeneratorConfiguration();
        String targetPackage = generatorConfiguration.getTargetPackage();
        element.addAttribute(new Attribute("parameterType", "java.lang.Long"));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("    delete from ").append(tableName).append("\n");
        sqlBuilder.append("   where id = #{id,jdbcType=BIGINT}\n");
        element.addElement(new TextElement(sqlBuilder.toString()));
        parentElement.addElement(element);
    }
}
