package com.lgn.dlock.support;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DLock {

    /**锁名称，不写默认类名称+方法名称*/
    String name() default "";

    /**拼接在name后，实现细粒度锁*/
    String expression() default "";

    /**获取锁时：是否阻塞等待*/
    boolean isBlock() default false;

    /**阻塞等待超时时间：秒*/
    long timeout() default 0;
}
