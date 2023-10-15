package top.zynorl.mrrtx.dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import top.zynorl.mrrtx.DBContextHolder;
import top.zynorl.mrrtx.constant.WriteOrReadEnum;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by zynorl on 2023/9/14 10:17
 */
//实现@code{AbstractRoutingDataSource}， 动态数据源获取，每当切换数据源，都要从这个里面进行获取
public class DynamicDataSource extends AbstractRoutingDataSource  implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSource.class);

    private static String defaultDBKey;

    @Value("${zynorl-db-router.datasource.default}")
    public  void setDefaultDBKey(String dbKey) {
        defaultDBKey = dbKey;
    }

//    private static DBGroup dbGroup;
//
//    @Autowired //@Autowired作用在普通方法上，会在注入该类的时候调用一次该方法
//    public  void setDBGroupsEntity(DBGroup dbGroup) {
//        DynamicDataSource.dbGroup = dbGroup;
//    }

    /**
     * 构建动态数据源
     * @param defaultTargetDataSource 默认的数据源对象
     * @param targetDataSources 全部的数据源对象
     */
    public DynamicDataSource(DataSource defaultTargetDataSource,
                             Map<Object, Object> targetDataSources) {
        super.setDefaultTargetDataSource(defaultTargetDataSource); // 调用父类方法
        super.setTargetDataSources(targetDataSources); // 调用父类方法
        super.afterPropertiesSet(); // 属性的设置
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return determineCurrentDataSource();
    }

    public static String determineCurrentDataSource() {
        String writeOrRead = DBContextHolder.getWriteOrRead();
        DBContextHolder.clearWriteOrRead();
        String masterKey = DBContextHolder.getMasterDBKey();
        String slaverKey = DBContextHolder.getSlaverDBKey();
        String tbIdx = DBContextHolder.getTBKey();
        // 默认数据源
        if(masterKey==null){
            logger.debug("未配置数据库路由，使用默认数据库：{}， 数据表: {}", defaultDBKey, tbIdx);
            return defaultDBKey;
        }
        // 读库，slaves
        if(writeOrRead!=null&&!writeOrRead.isEmpty()&&writeOrRead.equals(WriteOrReadEnum.READ.getValue())){
            logger.debug("读操作，数据库路由：{} 数据表：{}", slaverKey, tbIdx);
            DBContextHolder.clearSlaverDBKey();
            DBContextHolder.clearMasterDBKey();
            return slaverKey;
        }else{ //写库，master  如果没有配置读写分离，默认从主库读取
            logger.debug("写操作，数据库路由：{} 数据表：{}", masterKey, tbIdx);
            DBContextHolder.clearSlaverDBKey();
            DBContextHolder.clearMasterDBKey();
            return masterKey;
        }
    }

}
