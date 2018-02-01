package com.zeros.mybatis;

import com.zeros.mybatis.plugin.PageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2018/2/1.
 *
 * @author 迹_Jason
 */
@Configuration
public class ZerosMybatisAutoConfigure {
    @Bean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }
}
