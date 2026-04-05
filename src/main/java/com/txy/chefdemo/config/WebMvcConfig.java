package com.txy.chefdemo.config;

import com.txy.chefdemo.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * 配置全局跨域规则。
     * 这里允许前端从不同端口访问后端接口，比如本地开发时前端是 5173，后端是 8080。
     * 否则浏览器会因为同源策略拦截请求。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册登录鉴权拦截器。
     * 含义是：除了注册、登录、常量接口外，其余请求都要先经过 authInterceptor 校验 token，
     * 校验通过后才会真正进入 controller。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 除了 excludePathPatterns 不需要 authInterceptor 校验，其他都需要
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/register", "/auth/login", "/constant/**");
    }
}
