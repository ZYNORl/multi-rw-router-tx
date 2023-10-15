package top.zynorl.mrrtx.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zynorl on 2023/9/18 19:59
 */

@Component
@Aspect
public class TransactionAspect { // 事务开启切面类

    private static final int TRANSACTION_METHOD_TIMEOUT = 5; // 事务处理的超时时间
    private static final String AOP_POINTCUT_EXPRESSION = "@annotation(top.zynorl.mrrtx.annotation.MultiDBTransaction)";

    private final TransactionManager transactionManager; // 事务管理器

    public TransactionAspect(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    private TransactionInterceptor transactionInterceptorConfig() {
        // 配置数据读取事务规则
        RuleBasedTransactionAttribute readOnlyAttribute = new RuleBasedTransactionAttribute();
        readOnlyAttribute.setReadOnly(true); // 只读事务
        readOnlyAttribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED); // 非事务运行
        // 配置了数据更新事务规则
        RuleBasedTransactionAttribute requiredAttribute = new RuleBasedTransactionAttribute();
        /*
         * 事务开启 PROPAGATION_REQUIRED
         * 在当前方法中使用事务，当前方法的子方法也包含在事务中，如果当前方法的调用方也包含一个事务，则和调用方共用一个事务
         * 即使子方法没有加事务注解，也被包含在事务中
         */
        requiredAttribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        requiredAttribute.setTimeout(TRANSACTION_METHOD_TIMEOUT); // 事务处理超时时间
        // 配置所有要进行事务处理的方法名称定义
        Map<String, TransactionAttribute> transactionAttributeMap = new HashMap<>();
        transactionAttributeMap.put("*", requiredAttribute); // *,insert*,edit*,delete*,list*,get*
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        source.setNameMap(transactionAttributeMap); // 配置方法名称的映射
        return new TransactionInterceptor(transactionManager, source);
    }
    @Bean("txAdvisor")
    public Advisor transactionAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, transactionInterceptorConfig()); // 直接使用方法注入配置项
    }
}

