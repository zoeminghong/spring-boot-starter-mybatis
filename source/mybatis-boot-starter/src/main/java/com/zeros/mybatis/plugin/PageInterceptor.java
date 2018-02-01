package com.zeros.mybatis.plugin;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created on 2018/1/31.
 *
 * @author 迹_Jason
 */
@SuppressWarnings("Duplicates")
@Intercepts(
        {
                @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
                @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
        })
public class PageInterceptor implements Interceptor {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (MybatisPageContext.pageable()) {
            // 分页参数
            int index = MybatisPageContext.getPageRequest().getPageNumber();
            int pageSize = MybatisPageContext.getPageRequest().getPageSize();

            // 对应SQL limit限制参数
            int offset = (index - 1) * pageSize;
            int limit = pageSize;

            // SQL声明及分页总数统计处理
            if (invocation.getTarget() instanceof StatementHandler) {
                StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
                MetaObject metaStatementHandler = SystemMetaObject.forObject(statementHandler);
                // 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环
                // 可以分离出最原始的的目标类)
                while (metaStatementHandler.hasGetter("h")) {
                    Object object = metaStatementHandler.getValue("h");
                    metaStatementHandler = SystemMetaObject.forObject(object);
                }
                // 分离最后一个代理对象的目标类
                while (metaStatementHandler.hasGetter("target")) {
                    Object object = metaStatementHandler.getValue("target");
                    metaStatementHandler = SystemMetaObject.forObject(object);
                }
                MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
                // 原始SQL
                BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
                String sql = boundSql.getSql();
                // 获取总页数，通过JDBC执行SQL
                String countSQL = MessageFormat.format("SELECT COUNT(*) FROM ({0}) aliasForPage", sql);
                BoundSql countBoundSql = new BoundSql(mappedStatement.getConfiguration(), countSQL, boundSql.getParameterMappings(), boundSql.getParameterObject());
                try (
                        Connection connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
                        PreparedStatement countStatement = connection.prepareStatement(countSQL)
                ) {
                    MappedStatement stmt = copyFromMappedStatement(mappedStatement, new PageInterceptor.BoundSqlSqlSource(countBoundSql));
                    setParameters(countStatement, stmt, boundSql, boundSql.getParameterObject());
                    try (
                            ResultSet resultSet = countStatement.executeQuery()
                    ) {
                        int totalSize = 0;
                        while (resultSet.next()) {
                            totalSize = resultSet.getInt(1);
                        }
                        // page属性设置
                        MybatisPageContext.setTotalSize(totalSize);
                    }
                }
                // 页码转换
                StringBuilder sqlStringBuilder = new StringBuilder(sql);
                sqlStringBuilder.append(" limit ").append(offset).append(" , ").append(limit);
                BoundSql limitBoundSql = new BoundSql(mappedStatement.getConfiguration(), sqlStringBuilder.toString(), boundSql.getParameterMappings(), boundSql.getParameterObject());
                for (ParameterMapping mapping : boundSql.getParameterMappings()) {
                    String prop = mapping.getProperty();
                    if (boundSql.hasAdditionalParameter(prop)) {
                        limitBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
                    }
                }
                // 修改Statement
                metaStatementHandler.setValue("delegate.boundSql.sql", sqlStringBuilder.toString());
                return invocation.proceed();
            }
            // 分页查询结果处理
            List result = (List) invocation.proceed();
            if (result == null) {
                result = new ArrayList<>();
            }
            MybatisPageContext.setPageResult(result);
            return result;
        } else {
            return invocation.proceed();
        }
    }

    private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) throws SQLException {
        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() == ParameterMode.OUT){
                    continue;
                }
                Object value;
                String propertyName = parameterMapping.getProperty();
                PropertyTokenizer prop = new PropertyTokenizer(propertyName);
                if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
                    value = boundSql.getAdditionalParameter(prop.getName());
                    if (value != null) {
                        value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
                    }
                } else {
                    value = metaObject == null ? null : metaObject.getValue(propertyName);
                }
                TypeHandler typeHandler = parameterMapping.getTypeHandler();
                if (typeHandler == null) {
                    logger.error("There was no TypeHandler found for parameter " + propertyName + " of statement " + mappedStatement.getId());
                    throw new ExecutorException("There was no TypeHandler found for parameter " + propertyName + " of statement " + mappedStatement.getId());
                }
                typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
            }
        }
    }

    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    public static class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
