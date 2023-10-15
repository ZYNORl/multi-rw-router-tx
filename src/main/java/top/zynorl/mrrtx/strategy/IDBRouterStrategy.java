package top.zynorl.mrrtx.strategy;

/**
 * Created by zynorl on 2023/9/18 19:46
 *
 * 路由策略
 */
public interface IDBRouterStrategy {

    /**
     * 路由计算
     *
     * @param dbKeyAttr 路由字段
     */
    void doRouter(String dbKeyAttr);

    /**
     * 清除路由
     */
    void clear();

}
