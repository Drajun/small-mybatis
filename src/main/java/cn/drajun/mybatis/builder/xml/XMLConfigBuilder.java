package cn.drajun.mybatis.builder.xml;


import cn.drajun.mybatis.builder.BaseBuilder;
import cn.drajun.mybatis.datasource.DataSourceFactory;
import cn.drajun.mybatis.io.Resources;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.Environment;
import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.mapping.SqlCommandType;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.transaction.TransactionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML配置构建器
 */
public class XMLConfigBuilder extends BaseBuilder {

    private Element root;

    public XMLConfigBuilder(Reader reader){
        // 调用父类初始化Configuration
        super(new Configuration());

        // 获取XML
        SAXReader saxReader = new SAXReader();
        try{
            Document document = saxReader.read(new InputSource(reader));
            root = document.getRootElement();
        }
        catch (DocumentException e){
            e.printStackTrace();
        }
    }

    /**
     * 解析配置
     * @return
     */
    public Configuration parse(){
        try {
            // 解析和创建环境
            environmentsElement(root.element("environments"));

            // 所有的mapper（定义了sql的xml文件）的路径都写在“mybatis-config-datasource.xml”文件的mappers节点中
            mapperElement(root.element("mappers"));
        }
        catch (Exception e){
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause:" + e, e);
        }
        return configuration;
    }

    /**
     * 解析XML中配置的环境参数，根据参数创建环境
     * @param context
     * @throws Exception
     */
    private void environmentsElement(Element context) throws Exception{
        String environment = context.attributeValue("default");

        List<Element> environmentList = context.elements("environment");
        for(Element e : environmentList){
            String id = e.attributeValue("id");
            if(environment.equals(id)){
                // 事务管理器
                TransactionFactory txFactory = (TransactionFactory) typeAliasRegistry.resolveAlias(e.element("transactionManager").attributeValue("type")).newInstance();

                // 数据源
                Element dataSourceElement = e.element("dataSource");
                DataSourceFactory dataSourceFactory = (DataSourceFactory) typeAliasRegistry.resolveAlias(dataSourceElement.attributeValue("type")).newInstance();
                List<Element> propertyList = dataSourceElement.elements("property");
                Properties props = new Properties();
                for(Element property : propertyList){
                    props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
                }
                dataSourceFactory.setProperties(props);
                DataSource dataSource = dataSourceFactory.getDataSource();

                // 构建环境
                Environment.Builder environmentBuilder = new Environment.Builder(id).transactionFactory(txFactory).dataSource(dataSource);

                configuration.setEnvironment(environmentBuilder.build());
            }
        }
    }

    /**
     * 具体的解析操作
     * @param mappers
     * @throws Exception
     */
    private void mapperElement(Element mappers) throws Exception{
        List<Element> mapperList = mappers.elements("mapper");
        for(Element e : mapperList){
            String resource = e.attributeValue("resource");
            String mapperClass = e.attributeValue("class");

            // XML配置方式
            if(resource != null && mapperClass == null){
                InputStream inputStream = Resources.getResourceAsStream(resource);
                // 在for循环里每个mapper都重新new一个XMLMapperBuilder，来解析
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource);
                mapperParser.parse();
            }
            // 注解配置方式
            else if(resource == null && mapperClass != null){
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                configuration.addMapper(mapperInterface);
            }



        }
    }
}
