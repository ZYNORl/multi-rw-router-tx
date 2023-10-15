package top.zynorl.mrrtx.strategy.impl;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Component;
import top.zynorl.mrrtx.DBContextHolder;
import top.zynorl.mrrtx.entity.DBGroup;
import top.zynorl.mrrtx.strategy.IDBRouterStrategy;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zynorl on 2023/9/18 19:46
 *
 * 哈希路由策略
 */
@Component
public class DBRouterStrategyHashCode implements IDBRouterStrategy {

    private final DBGroup dbGroup;

    public DBRouterStrategyHashCode(DBGroup dbGroup) {
        this.dbGroup = dbGroup;
    }

    @Override
    public void doRouter(String dbKeyAttr) {
        AtomicInteger size = new AtomicInteger(0);
        Map<String, Integer> dbToTBCount = dbGroup.dbToTBCount;
        dbToTBCount.forEach((dbName, tbCount) -> size.addAndGet(tbCount));
        // 扰动函数；在 JDK 的 HashMap 中，对于一个元素的存放，需要进行哈希散列。而为了让散列更加均匀，所以添加了扰动函数。
        int idx = ((size.get() - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16)))+1;

        // 库表索引；相当于是把一个长条的桶，切割成段，对应分库分表中的库编号和表编号
        // 公式目的；8个位置，计算出来的是位置在5 那么你怎么知道5是在2库1表。
        int dbIdx = 0;
        int tbIdx = 0;
        int tbTotal = 0;
        for (Map.Entry<String, Integer> entry : dbToTBCount.entrySet()) {
            tbTotal += entry.getValue();
            dbIdx += 1;
            if (idx <= tbTotal) {
                tbIdx = entry.getValue() - (tbTotal - idx);
                break;
            }
        }
        // 设置到ThreadLocal
        DBContextHolder.setMasterDBKey(dbGroup.masterDBs.get(dbIdx-1));
        String[] slavers = dbGroup.masterTOSlaves.get(DBContextHolder.getMasterDBKey());
        int i = RandomUtils.nextInt(slavers.length); // 从库，随机选取
        DBContextHolder.setSlaverDBKey(slavers[i]);
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
    }

    @Override
    public void clear(){
        DBContextHolder.clearMasterDBKey();
        DBContextHolder.clearSlaverDBKey();
        DBContextHolder.clearTBKey();
    }

}
