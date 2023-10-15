package top.zynorl.mrrtx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import top.zynorl.mrrtx.entity.DBGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zynorl on 2023/9/16 14:57
 */
@Configuration
public class DBGroupConfig {

    @Component("myGroup")
    @ConfigurationProperties(prefix = "zynorl-db-router.datasource")
    public static class MyGroup{
        private static List<Map<String, Object>> groupEntities;
        //ConfigurationProperties, 只会调用非静态的set方法对变量进行配置。Lombook插件@Data注解生成的setter方法默认是非静态的
        public void setGroups(List<Map<String, Object>> groups) {
            groupEntities = groups;
        }
    }


    @Bean
    @DependsOn("myGroup")
    public DBGroup dbGroup(){
        DBGroup dbGroup = new DBGroup();
        dbGroup.masterDBs = new ArrayList<>();
        dbGroup.masterTOSlaves = new HashMap<>();
        dbGroup.dbToTBCount = new HashMap<>();
        MyGroup.groupEntities.forEach(mapEntity->{
            dbGroup.masterDBs.add((String) mapEntity.get("master"));
            String[] slavers = mapEntity.get("slavers").toString().split(",");
            dbGroup.masterTOSlaves.put((String) mapEntity.get("master"), slavers);
            dbGroup.dbToTBCount.put((String) mapEntity.get("master"), (Integer) mapEntity.get("tbCount"));
        });
        return dbGroup;
    }

}