package top.zynorl.mrrtx.config;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import top.zynorl.mrrtx.dynamic.DynamicDataSource;
import top.zynorl.mrrtx.dynamic.plugin.DynamicExecutorRWMybatisPlugin;
import top.zynorl.mrrtx.dynamic.plugin.DynamicHandlerMybatisPlugin;
import top.zynorl.mrrtx.util.PropertyUtil;
import top.zynorl.mrrtx.util.StringUtils;

import javax.sql.DataSource;
import java.util.*;

/**
 * Created by zynorl on 2023/9/18 19:51
 */

@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {

    /**
     * 分库全局属性
     */
    private static final String TAG_GLOBAL = "global";

    /**
     * 默认数据源名称
     */
    private String defaultDataSourceKey = "";


    /**
     * XA数据源配置组
     */
    private Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

    /**
     * 默认数据源配置
     */
    private Map<String, Object> defaultDataSourceProps;


    @Bean
    public Interceptor[] plugin() {
        Interceptor[] inters = new Interceptor[2];
        inters[0] = new DynamicHandlerMybatisPlugin();
        inters[1] = new DynamicExecutorRWMybatisPlugin();
        return inters;
    }


    @Bean
    public DataSource createXADataSource() {
        // 创建数据源
        Map<Object, Object> targetDataSources = new HashMap<>();
        for (String dbKey : dataSourceMap.keySet()) {
            Map<String, Object> objMap = dataSourceMap.get(dbKey);
            // 根据objMap创建DataSourceProperties,遍历objMap根据属性反射创建DataSourceProperties
            DataSource ds = createXADataSource(dbKey, objMap);
            targetDataSources.put(dbKey, ds);
        }
        // 设置数据源  db00为默认数据源
        return new DynamicDataSource(createXADataSource(defaultDataSourceKey, defaultDataSourceProps), targetDataSources);
    }

    private DataSource createXADataSource(String dbKey, Map<String, Object> attributes) {
        DruidXADataSource xaDatasource = new DruidXADataSource();
        xaDatasource.setUrl((String) attributes.get("url"));
        xaDatasource.setUsername((String) attributes.get("username"));
        xaDatasource.setPassword(attributes.get("password").toString());
        xaDatasource.setDriverClassName((String) attributes.get("driverClassName"));
        xaDatasource.setInitialSize((Integer) attributes.get("initialSize"));
        xaDatasource.setMinIdle((Integer) attributes.get("minIdle"));
        xaDatasource.setMaxActive((Integer) attributes.get("maxActive"));
        xaDatasource.setMaxWait((Integer) attributes.get("maxWait"));
        xaDatasource.setTimeBetweenEvictionRunsMillis((Integer) attributes.get("timeBetweenEvictionRunsMillis"));
        xaDatasource.setMinEvictableIdleTimeMillis((Integer)attributes.get("minEvictableIdleTimeMillis"));
        xaDatasource.setValidationQuery((String) attributes.get("validationQuery"));
        xaDatasource.setTestWhileIdle((Boolean) attributes.get("testWhileIdle"));
        xaDatasource.setTestOnBorrow((boolean) attributes.get("testOnBorrow"));
        xaDatasource.setTestOnReturn((Boolean) attributes.get("testOnReturn"));
        xaDatasource.setPoolPreparedStatements((Boolean) attributes.get("poolPreparedStatements"));
        xaDatasource.setMaxPoolPreparedStatementPerConnectionSize((Integer) attributes.get("maxPoolPreparedStatementPerConnectionSize"));
        AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
        atomikosDataSourceBean.setXaDataSource(xaDatasource);
        atomikosDataSourceBean.setUniqueResourceName(dbKey);
        return atomikosDataSourceBean;
    }


    @Override
    public void setEnvironment(@Autowired Environment environment) {
        String prefix = "zynorl-db-router.datasource.";
        Map<String, Object> globalInfo = getGlobalProps(environment, prefix + TAG_GLOBAL);
        Map<String, Map<String, Object>> propertyDBGroups = PropertyUtil.handle(environment, prefix + "groups", Map.class);

        propertyDBGroups.forEach((String key, Map<String, Object> DBGroup)->{
            String masterKey = (String) Objects.requireNonNull(DBGroup.get("master"));
            String slaves = (String) Objects.requireNonNull(DBGroup.get("slavers"));
            String[] slaveKeys = slaves.split(",");
            String masterDBPrefix = prefix + masterKey;
            Map<String, Object> masterDataSourceProps = PropertyUtil.handle(environment, masterDBPrefix, Map.class);
            injectGlobal(masterDataSourceProps, globalInfo);
            dataSourceMap.put(masterKey, masterDataSourceProps);
            for (String slaveKey : slaveKeys) {
                String slaveDBPrefix = prefix + slaveKey;
                Map<String, Object> slaveDataSourceProps = PropertyUtil.handle(environment, slaveDBPrefix, Map.class);
                injectGlobal(slaveDataSourceProps, globalInfo);
                dataSourceMap.put(slaveKey, slaveDataSourceProps);
            }
        });

        // 默认数据源
        defaultDataSourceKey = environment.getProperty(prefix + "default");
        defaultDataSourceProps = PropertyUtil.handle(environment, prefix + defaultDataSourceKey, Map.class);
        injectGlobal(defaultDataSourceProps, globalInfo);
    }

    private Map<String, Object> getGlobalProps(Environment environment, String key) {
        try {
            return PropertyUtil.handle(environment, key, Map.class);
        } catch (Exception e) {
            return Collections.EMPTY_MAP;
        }
    }


    private void injectGlobal(Map<String, Object> origin, Map<String, Object> global) {
        for (String key : global.keySet()) {
            if(global.get(key) instanceof Map){
                injectGlobal(origin, (Map<String, Object>) global.get(key));
            }else{
                String camelKey = StringUtils.middleScoreToCamelCase(key);
                if (!origin.containsKey(camelKey)) {
                    origin.put(camelKey, global.get(key));
                }
            }
        }
        adjustDataSourceFormat(origin);
    }

    private void adjustDataSourceFormat(Map<String, Object> DataSourceMap) {
        Set<String> keySet = new HashSet<>(DataSourceMap.keySet());
        for (String key : keySet) {
            if(!StringUtils.middleScoreToCamelCase(key).equals(key)){
                DataSourceMap.put(StringUtils.middleScoreToCamelCase(key), DataSourceMap.remove(key));
            }
        }
    }
}

