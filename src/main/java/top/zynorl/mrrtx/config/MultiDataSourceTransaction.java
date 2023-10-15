package top.zynorl.mrrtx.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.zynorl.mrrtx.dynamic.DynamicDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code MultiDataSourceTransaction} handles the lifecycle of a JDBC connection. It retrieves a connection from Spring's
 * transaction manager and returns it back to it when it is no longer needed.
 * <p>
 * If Spring's transaction handling is active it will no-op all commit/rollback/close calls assuming that the Spring
 * transaction manager will do the job.
 * <p>
 * If it is not it will behave like {@code JdbcTransaction}.
 *
 * Created by zynorl on 2023/9/18 19:59
 */
// 在当前线程下，为了能够实现在整个事务控制中，可以根据DatabaseType获取不同的Connection进行动态的数据源切换，需要重写Transaction
// 大多项目将MyBatis/MyBatisPlus作为其ORM持久层框架，所以本插件整合Mybatis，此时一定要基于MyBatis标准开发事务管理器
// 参照{@code SpringManagedTransaction}
public class MultiDataSourceTransaction
        implements org.apache.ibatis.transaction.Transaction {

    private static final Logger logger = LoggerFactory.getLogger(MultiDataSourceTransaction.class);

    private DataSource dataSource; // 事务是需要有DataSource支持
    private Connection currentConnection; // 当前的数据库连接
    private String currentDatabaseName; // 当前数据库名称
    private boolean autoCommit; // 是否要进行自动提交启用
    private boolean isConnectionTransactional; // 是否要启用新的事务
    private ConcurrentHashMap<String, Connection> otherConnectionMap; // 保存其他的Connection对象
    public MultiDataSourceTransaction(DataSource dataSource) {
        this.dataSource = dataSource; // 保存数据元
        this.otherConnectionMap = new ConcurrentHashMap<>(); // 保存其他数据库连接
    }
    private void openCurrentConnection(String datasourceName, DataSource dataSource) throws SQLException { // 打开一个连接
        // 通过当前得到的DataSource接口实例来获取一个Connection接口实例
        this.currentConnection = DataSourceUtils.getConnection(dataSource);
        this.currentDatabaseName = datasourceName; // 保存当前的数据库名称
        this.otherConnectionMap.put(datasourceName, this.currentConnection); // 保存连接
        this.autoCommit = this.currentConnection.getAutoCommit(); // 获取当前的是否自动提交的状态
        this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.currentConnection, dataSource);
        System.out.println(currentConnection.getMetaData());
        logger.info("当前数据库连接：{}、事务支持状态：{}。", this.currentConnection, this.isConnectionTransactional);
    }
    private DataSource getDataSourceByName(String datasourceName){
        Map<Object,Object> targetDataSource = new HashMap<>();
        try {
            Field targetDataSourcesField = dataSource.getClass().getSuperclass().getDeclaredField("targetDataSources");
            targetDataSourcesField.setAccessible(true);
            targetDataSource = (Map<Object, Object>)targetDataSourcesField.get(dataSource);
            targetDataSourcesField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (DataSource) targetDataSource.get(datasourceName);
    }
    /**
     * Gets a connection from Spring transaction manager and discovers if this {@code Transaction} should manage
     * connection or let it to Spring.
     * <p>
     * It also reads autocommit setting because when using Spring Transaction MyBatis thinks that autocommit is always
     * false and will always call commit/rollback so we need to no-op that calls.
     */
    @Override
    public Connection getConnection() throws SQLException { // 获取数据库连接
        logger.info("当前事务名称：{}", TransactionSynchronizationManager.getCurrentTransactionName());
        // 存在有数据源的前提下才可以实现连接的获取，那么首先要判断是否有数据源存在
        String datasourceName = DynamicDataSource.determineCurrentDataSource(); // 获取当前数据源名称
        logger.info("【MultiDataSourceTransaction.getConnection()】数据源名称：{}", datasourceName);
        if (null == currentDatabaseName || datasourceName.equals(this.currentDatabaseName)) {    // 当前还没有使用数据库 或 现在的数据源为当前使用的数据库
            if(this.currentConnection == null){ // 如果当前的数据源没有开启过连接
                DataSource currDataSource = getDataSourceByName(datasourceName);
                openCurrentConnection(datasourceName, currDataSource); // 开启一个数据库连接
            }
        } else {    // 现在要切换使用的数据源不是当前正在使用的数据库
            if (!this.otherConnectionMap.containsKey(datasourceName)) { // 现在要切换使用的数据源没有建立连接
                if(dataSource instanceof DynamicDataSource){
                    DataSource currDataSource = getDataSourceByName(datasourceName);
                    openCurrentConnection(datasourceName, currDataSource); // 开启一个数据库连接
                }else{
                    DataSource currDataSource = getDataSourceByName(datasourceName);
                    openCurrentConnection(datasourceName, currDataSource); // 开启一个数据库连接
                }
            }
        }
        return this.currentConnection; // 现在要切换使用的数据源已经建立好了数据库连接，返回他的连接
    }

    /**
     * 1. 在事务状态下，无法提交
     * 2. mybatis默认autoCommit是false, 事务提交任务，the Spring transaction manager will do the job.
     * If it is not it will behave like {@code JdbcTransaction}.
     * 3. {@code this.currentConnection.getAutoCommit()=>NativeServerSession->isAutoCommit(boolean)}
     * MySQL的默认提交事务是指在执行单条SQL语句时，是否自动将其作为一个独立的事务提交给数据库,
     * 默认情况下是开启的: 一条sql语句就是一个事务。
     */
    @Override
    public void commit() throws SQLException { // 数据库事务提交
        logger.info("commit：currentConnection = {}、isConnectionTransactional = {}、autoCommit = {}", this.currentConnection, this.isConnectionTransactional, this.autoCommit);
        // 当前存在有Connection接口实例，同时没有开启自动的事务提交，并且存在有支持事务的连接
        if (this.currentConnection != null && !this.isConnectionTransactional && !this.autoCommit) {
            logger.info("数据库事务提交，当前数据库连接：{}", this.currentConnection);
            this.currentConnection.commit(); // 提交当前的数据库事务
            for (Connection connection : this.otherConnectionMap.values()) { // 控制其它的数据库连接
                connection.commit(); // 保证其他的连接提交事务
            }
        }
    }

    @Override
    public void rollback() throws SQLException { // 事务回滚
        logger.error("rollback：currentConnection = {}、isConnectionTransactional = {}、autoCommit = {}", this.currentConnection, this.isConnectionTransactional, this.autoCommit);
        if (this.currentConnection != null && !this.isConnectionTransactional && !this.autoCommit) {
            logger.info("数据库事务回滚，当前数据库连接：{}", this.currentConnection);
            this.currentConnection.rollback(); // 回滚当前的数据库事务
            for (Connection connection : this.otherConnectionMap.values()) { // 控制其它的数据库连接
                connection.rollback(); // 保证其他的连接提交回滚
            }
        }
    }

    @Override
    public void close() { // 事务关闭
        DataSourceUtils.releaseConnection(this.currentConnection, this.dataSource);
        for (Connection connection : this.otherConnectionMap.values()) { // 控制其它的数据库连接
            DataSourceUtils.releaseConnection(connection, this.dataSource);
        }
    }

    @Override
    public Integer getTimeout() { // 超时配置
        int seconds = 0;
        for (String datasourceName : otherConnectionMap.keySet()) {
            DataSource dataSourceByName = getDataSourceByName(datasourceName);
            ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSourceByName);
            if (holder != null && holder.hasTimeout()) {
                seconds = holder.getTimeToLiveInSeconds()>seconds?holder.getTimeToLiveInSeconds():seconds;
                return seconds;
            }
        }
        return seconds;
    }
}
