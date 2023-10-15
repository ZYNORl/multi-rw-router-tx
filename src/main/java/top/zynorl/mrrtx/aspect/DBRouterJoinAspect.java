package top.zynorl.mrrtx.aspect;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.zynorl.mrrtx.annotation.DBRouter;
import top.zynorl.mrrtx.strategy.IDBRouterStrategy;

import java.lang.reflect.Field;

/**
 * Created by zynorl on 2023/9/10 15:59
 */
@Aspect
@Component
public class DBRouterJoinAspect {

    private Logger logger = LoggerFactory.getLogger(DBRouterJoinAspect.class);


    private final IDBRouterStrategy dbRouterStrategy;

    public DBRouterJoinAspect(IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterStrategy = dbRouterStrategy;
    }

    @Pointcut("@annotation(top.zynorl.mrrtx.annotation.DBRouter)")
    public void aopPoint() {
    }

    /**
     * 所有需要分库分表的操作，都需要使用自定义注解进行拦截，拦截后读取方法中的入参字段，根据字段进行路由操作。
     * 1. dbRouter.key() 确定根据哪个字段进行路由
     * 2. getAttrValue 根据数据库路由字段，从入参中读取出对应的值。比如路由 key 是 uId，那么就从入参对象 Obj 中获取到 uId 的值。
     * 3. dbRouterStrategy.doRouter(dbKeyAttr) 路由策略根据具体的路由值进行处理
     * 4. 路由处理完成比，就是放行。 jp.proceed();
     * 5. 最后 dbRouterStrategy 需要执行 clear 因为这里用到了 ThreadLocal 需要手动清空。
     */
    @Around("aopPoint() && @annotation(methodDBRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter methodDBRouter) throws Throwable {
        Object target = jp.getTarget();
        MetaObject metaObject = MetaObject.forObject(target, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        String interfaceName = (String)metaObject.getValue("h.mapperInterface.name");
        DBRouter classDBRouter = Class.forName(interfaceName).getAnnotation(DBRouter.class);
        if(methodDBRouter!=null && !StringUtils.isBlank(methodDBRouter.key())){// 判断方法注解上的路由，方法优先级高于类
            // 路由属性
            String dbKeyAttr = getAttrValue(methodDBRouter.key(), jp.getArgs());
            // 路由策略
            dbRouterStrategy.doRouter(dbKeyAttr);
        }else if(classDBRouter!=null && !StringUtils.isBlank(classDBRouter.key())){// 判断类注解上的路由
            // 路由属性
            String dbKeyAttr = getAttrValue(classDBRouter.key(), jp.getArgs());
            // 路由策略
            dbRouterStrategy.doRouter(dbKeyAttr);
        }else{
            throw new RuntimeException("annotation DBRouter key is null！");
        }
        // 返回结果
        return jp.proceed();
    }

    private String getAttrValue(String attr, Object[] args) {
        if (1 == args.length) {
            Object arg = args[0];
            if (arg instanceof String) {
                return arg.toString();
            }
        }

        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                // filedValue = BeanUtils.getProperty(arg, attr);
                // fix: 使用lombok时，uId这种字段的get方法与idea生成的get方法不同，会导致获取不到属性值，改成反射获取解决
                filedValue = String.valueOf(this.getValueByName(arg, attr));
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }

    /**
     * 获取对象的特定属性值
     */
    private Object getValueByName(Object item, String name) {
        try {
            Field field = getFieldByName(item, name);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(item);
            field.setAccessible(false);
            return o;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 根据名称获取方法，该方法同时兼顾继承类获取父类的属性
     */
    private Field getFieldByName(Object item, String name) {
        try {
            Field field;
            try {
                field = item.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                field = item.getClass().getSuperclass().getDeclaredField(name);
            }
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

}
