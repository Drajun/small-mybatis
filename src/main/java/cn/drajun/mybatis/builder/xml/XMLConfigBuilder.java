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
        // mappers中一个个mapper节点的resource属性实质是记录了mapper的文件路径
        List<Element> mapperList = mappers.elements("mapper");
        for(Element e : mapperList){
            String resource = e.attributeValue("resource");
            Reader reader = Resources.getResourceAsReader(resource);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new InputSource(reader));
            Element root = document.getRootElement();

            // 命名空间
            String namespace = root.attributeValue("namespace");

            // XML文件中的select节点
            List<Element> selectNodes = root.elements("select");
            for(Element node : selectNodes){
                String id = node.attributeValue("id");
                String parameterType = node.attributeValue("parameterType");
                String resultType = node.attributeValue("resultType");
                String sql = node.getText();

                // SQL语句中的参数匹配
                Map<Integer, String> parameter = new HashMap<>();
                Pattern pattern = Pattern.compile("(#\\{(.*?)})");
                Matcher matcher = pattern.matcher(sql);
                for(int i = 1;matcher.find();i++){
                    // g1:#{id}, g2:id
                    String g1 = matcher.group(1);
                    String g2 = matcher.group(2);
                    parameter.put(i, g2);
                    sql = sql.replace(g1, "?");
                }

                // 封装SQL语句为对象
                String msId = namespace + "." + id;
                String nodeName = node.getName();
                SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

                BoundSql boundSql = new BoundSql(sql, parameter, parameterType, resultType);

                MappedStatement mappedStatement = new MappedStatement.Builder(configuration, msId, sqlCommandType, boundSql).build();

                //添加解析SQL
                configuration.addMappedStatement(mappedStatement);
            }

            // 注册Mapper映射器
            configuration.addMapper(Resources.classForName(namespace));
        }
    }
}
