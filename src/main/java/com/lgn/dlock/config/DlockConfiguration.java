package com.lgn.dlock.config;

import com.lgn.dlock.support.DLockInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(RedissonClient.class)
public class DlockConfiguration {

    @Bean
    public DLockInterceptor dlockInterceptor(){
        return new DLockInterceptor();
    }
}
