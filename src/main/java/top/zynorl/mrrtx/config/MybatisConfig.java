package top.zynorl.mrrtx.config;

import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import top.zynorl.mrrtx.dynamic.DynamicDataSource;

import javax.sql.DataSource;

@Configuration
public class MybatisConfig { // Mybatis拦截器配置return interceptor;
    @Primary
    @Bean
    public SqlSessionFactoryBean getSqlSessionFactory(
            @Autowired DataSource dataSource, // 要使用的数据源
            @Autowired Interceptor[] interceptors, // 配置mybatis拦截器
            @Value("${mybatis.config-location}") Resource configLocation, // 资源文件路径
            @Value("${mybatis.mapper-locations}") String mapperLocations // Mapping映射路径
    ) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        assert dataSource instanceof DynamicDataSource;

        sessionFactoryBean.setDataSource(dataSource); // 配置项目中要使用的数据源
        sessionFactoryBean.setVfs(SpringBootVFS.class); // 配置程序的扫描类
        sessionFactoryBean.setTransactionFactory(new MultiDataSourceTransactionFactory()); // 配置自定义事务工厂
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] mappings = resourcePatternResolver.getResources(mapperLocations);
        sessionFactoryBean.setMapperLocations(mappings);  // 配置Mapping映射路径
        sessionFactoryBean.setConfigLocation(configLocation);  // 配置资源文件路径
        sessionFactoryBean.setPlugins(interceptors); // 配置Mybatis拦截器

        return sessionFactoryBean;
    }
}
