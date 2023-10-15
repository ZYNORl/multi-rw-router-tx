package top.zynorl.mrrtx.entity;

import java.util.List;
import java.util.Map;

/**
 * Created by zynorl on 2023/9/18 19:46
 */
public class DBGroup {
    /**
     * 主库列表
     */
    public List<String> masterDBs;

    /**
     * 主数据库与从数据库的对应关系
     */
    public Map<String, String[]> masterTOSlaves;

    /**
     * 主数据库与表个数的对应关系,主数据库与它所对应的从数据库的表个数是一样的
     */
    public Map<String, Integer> dbToTBCount;
}
