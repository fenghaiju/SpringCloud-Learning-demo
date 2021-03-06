package com.zengjinhao.pos.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.servlet.KaptchaServlet;
import com.zengjinhao.pos.shiro.MyShiroRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.ExecutorServiceSessionValidationScheduler;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description  : Apache Shiro 核心通过 Filter 来实现，就好像SpringMvc 通过DispachServlet 来主控制一样。
 * 既然是使用 Filter 一般也就能猜到，是通过URL规则来进行过滤和权限校验，所以我们需要定义一系列关于URL的规则和访问权限。
 */

@Configuration
@Order(1)
public class ShiroConfig {


    /**
     * 凭证匹配器
     *
     * @return
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        //md5加密1次
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        hashedCredentialsMatcher.setHashIterations(1);
        return hashedCredentialsMatcher;
    }
    /**
     * 自定义realm
     *
     * @return
     */
    @Bean
    public MyShiroRealm userRealm() {
        MyShiroRealm userRealm = new MyShiroRealm();
        userRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return userRealm;
    }



    /**
     * 安全管理器
     * 注：使用shiro-spring-boot-starter 1.4时，返回类型是SecurityManager会报错，直接引用shiro-spring则不报错
     *
     * @return
     */
    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userRealm());
        return securityManager;
    }


    /**
     * 设置过滤规则
     *
     * @param securityManager
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setLoginUrl("/login");// 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setSuccessUrl("/");// 登录成功后要跳转的链接
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauth");// 未授权界面，不生效(详情原因看MyExceptionResolver)



        //anon:匿名拦截器，即不需要登录即可访问；一般用于静态资源过滤；示例“/static/**=anon”
        //authc:如“/**=authc”，基于表单的拦截器,如果没有登录会跳到相应的登录页面登录
        // 主要属性：usernameParam：表单提交的用户名参数名（ username）
        // passwordParam：表单提交的密码参数名（password）
        // rememberMeParam：表单提交的密码参数名（rememberMe）
        // loginUrl：登录页面地址（/login.jsp）
        // successUrl：登录成功后的默认重定向地址
        // failureKeyAttribute：登录失败后错误信息存储key（shiroLoginFailure）；


        //注意此处使用的是LinkedHashMap，是有顺序的，shiro会按从上到下的顺序匹配验证，匹配了就不再继续验证
        //所以上面的url要苛刻，宽松的url要放在下面，尤其是"/**"要放到最下面，如果放前面的话其后的验证规则就没作用了。
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/captcha.jpg", "anon");
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/**", "authc");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }




}
