package com.lgn.dlock.support;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
public class DLockInterceptor {

    ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Autowired
    RedissonClient redissonClient;

    public DLockInterceptor(){
        log.info("{} init.",this.getClass().getName());
    }

    @Pointcut(value="@annotation(com.lgn.dlock.support.DLock)")
    public void pointcut(){
        log.info("{} pointcut init.",this.getClass().getName());
    }

    @Around(value="pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        Object[] args = joinPoint.getArgs();
        Object target = joinPoint.getTarget();
        Signature signature = joinPoint.getSignature();
        if(!(signature instanceof MethodSignature)){
            log.error("{} is not MethodSignature, don't execute lock logic",signature.getClass().getName());
            return joinPoint.proceed(args);
        }
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if(!method.isAnnotationPresent(DLock.class)){
            log.error("{} is not DLock, don't execute lock logic.",signature.getName());
            return joinPoint.proceed(args);
        }
        log.info("{} start to execute lock logic...",signature.getName());
        RLock rlock = null;
        String lockName = null;
        try {
            DLock dLockAnnotaion = method.getAnnotation(DLock.class);
            lockName = dLockAnnotaion.name();
            if(lockName == null || lockName.isEmpty()){
                lockName = target.getClass().getName() + "." + signature.getName();
                log.info("{} Dlock name is empty,use default name: {}",signature.getName(),lockName);
            }
            String expression = dLockAnnotaion.expression();
            if(expression != null && !expression.isEmpty()){
                SpelExpressionParser parser = new SpelExpressionParser();
                Expression expr = parser.parseExpression(expression);
                Object value = expr.getValue(new MethodBasedEvaluationContext(target, method, args, discoverer));
                if(value != null && !value.toString().isEmpty()){
                    lockName += value;
                }
            }
            log.info("{} DLock name: {}",signature.getName(),lockName);
            rlock = redissonClient.getLock(lockName);
            if (dLockAnnotaion.isBlock()) {
                rlock.lock(dLockAnnotaion.timeout(), TimeUnit.SECONDS);
                log.info("{} lock[{}] success",signature.getName(),lockName);
                return joinPoint.proceed(args);
            }
            boolean tryLock = rlock.tryLock();
            if(tryLock){
                log.info("{} tryLock[{}] {}",signature.getName(),lockName,tryLock);
                return joinPoint.proceed(args);
            }
            return null;
        }catch (Throwable e) {
            log.error("{} execute lock logic error",signature.getName(),e);
            throw e;
        }finally {
            if(rlock != null) {
                rlock.unlock();
                log.info("{} unlock[{}]",signature.getName(),lockName);
            }
            log.info("{} finished to execute lock logic...",signature.getName());
        }
    }
}
