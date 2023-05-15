package cn.drajun.mybatis.scripting.defaults;

import cn.drajun.mybatis.executor.parameter.ParameterHandler;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.mapping.ParameterMapping;
import cn.drajun.mybatis.reflection.MetaObject;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.type.JdbcType;
import cn.drajun.mybatis.type.TypeHandler;
import cn.drajun.mybatis.type.TypeHandlerRegistry;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * 默认参数处理器
 */
public class DefaultParameterHandler implements ParameterHandler {

    private Logger logger = LoggerFactory.getLogger(DefaultParameterHandler.class);

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final MappedStatement mappedStatement;
    private final Object parameterObject;
    private BoundSql boundSql;
    private Configuration configuration;

    public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql){
        this.mappedStatement = mappedStatement;
        this.configuration = mappedStatement.getConfiguration();
        this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
    }

    @Override
    public Object getParameterObject() {
        return parameterObject;
    }

    @Override
    public void setParameters(PreparedStatement ps) throws SQLException {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if(null != parameterMappings){
            for(int i=0;i<parameterMappings.size();i++){
                /*
                设置参数，如果是单个基本类型的参数，可以直接赋值；
                如果参数是对象，则新建该对象的元对象，根据sql语句所需的参数从元对象中获取参数的值。
                 */
                ParameterMapping parameterMapping = parameterMappings.get(i);
                String propertyName = parameterMapping.getProperty();
                Object value;
                if(typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())){
                    value = parameterObject;
                }
                else{
                    // 通过 MetaObject.getValue 反射取得值设进去
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                JdbcType jdbcType = parameterMapping.getJdbcType();

                // 设置参数
                logger.info("根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value：{}", JSON.toJSONString(value));
                TypeHandler typeHandler = parameterMapping.getTypeHandler();
                typeHandler.setParameter(ps, i+1, value, jdbcType);
            }
        }
    }
}
