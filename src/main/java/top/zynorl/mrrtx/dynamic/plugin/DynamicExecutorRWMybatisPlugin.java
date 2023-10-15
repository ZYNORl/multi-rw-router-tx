package top.zynorl.mrrtx.dynamic.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import top.zynorl.mrrtx.DBContextHolder;
import top.zynorl.mrrtx.constant.WriteOrReadEnum;


/**
 * Created by zynorl on 2023/9/14 20:17
 */
@Intercepts(
        {@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
        }
        )
public class DynamicExecutorRWMybatisPlugin implements Interceptor {


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 拿到当前方法(update、query)所有参数
        Object[] objects = invocation.getArgs();

        // MappedStatement 封装CRUD所有的元素和SQL
        MappedStatement ms = (MappedStatement) objects[0];
        // 读方法
        if (ms.getSqlCommandType().equals(SqlCommandType.SELECT)) {

            DBContextHolder.setWriteOrRead(WriteOrReadEnum.READ.getValue());
        } else {
            // 写方法
            DBContextHolder.setWriteOrRead(WriteOrReadEnum.WRITE.getValue());
        }
        // 修改当前线程要选择的数据源的key
        return invocation.proceed();
    }
}