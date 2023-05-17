package cn.drajun.mybatis.builder.xml;

import cn.drajun.mybatis.builder.BaseBuilder;
import cn.drajun.mybatis.builder.MapperBuilderAssistant;
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

    // 映射器构建助手
    private MapperBuilderAssistant builderAssistant;

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource) throws DocumentException{
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    public XMLMapperBuilder(Document document, Configuration configuration, String resource){
        super(configuration);
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
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
            // 绑定映射器到namespace Mybatis 源码方法名 -> bindMapperForNamespace
            configuration.addMapper(Resources.classForName(builderAssistant.getCurrentNamespace()));
        }
    }

    /**
     * 配置mapper元素
     * @param element
     */
    private void configurationElement(Element element){
        // 配置namespace
        String namespace = element.attributeValue("namespace");
        if(namespace.equals("")){
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }
        builderAssistant.setCurrentNamespace(namespace);

        // 配置增删改查语句
        buildStatementFromContext(element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete")
        );
    }

    /**
     * 配置增删改查语句
     * @param lists
     */
    private void buildStatementFromContext(List<Element>... lists){
        for(List<Element> list : lists){
            for(Element element : list){
                final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, element);
                statementParser.parseStatementNode();
            }
        }
    }
}
