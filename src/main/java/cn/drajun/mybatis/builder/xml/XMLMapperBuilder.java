package cn.drajun.mybatis.builder.xml;

import cn.drajun.mybatis.builder.BaseBuilder;
import cn.drajun.mybatis.io.Resources;
import cn.drajun.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

/**
 * XML映射构造器
 */
public class XMLMapperBuilder extends BaseBuilder {

    private Element element;
    private String resource;
    private String currentNamespace;

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource) throws DocumentException{
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    public XMLMapperBuilder(Document document, Configuration configuration, String resource){
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
    }

    /**
     * 解析
     * @throws Exception
     */
    public void parse() throws Exception{
        // 解析未加载的资源
        if(!configuration.isResourceLoaded(resource)){
            configurationElement(element);
            configuration.addLoadedResource(resource);
            configuration.addMapper(Resources.classForName(currentNamespace));
        }
    }

    /**
     * 配置mapper元素
     * @param element
     */
    private void configurationElement(Element element){
        // 配置namespace
        currentNamespace = element.attributeValue("namespace");
        if(currentNamespace.equals("")){
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }

        // 配置增删改查语句
        buildStatementFromContext(element.elements("select"));
    }

    /**
     * 配置增删改查语句
     * @param list
     */
    private void buildStatementFromContext(List<Element> list){
        for(Element element : list){
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, element, currentNamespace);
            statementParser.parseStatementNode();
        }
    }
}
