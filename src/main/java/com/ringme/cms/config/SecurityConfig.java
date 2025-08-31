package com.ringme.cms.config;


import com.ringme.cms.service.sys.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.util.AntPathMatcher;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Value("${server.servlet.context-path}")
    private String CONTEXTPATH;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/login/**",
                        "/captcha.jpg",
                        "/generate-security",
                        "/auth-laoid/**",
                        "/music/crbt/tool-auto-upload-tone-from-excel",
                        "/music/crbt/tool-auto-download-avatar-and-ring-tone-from-excel",
                        "/static/**",
                        "/img/**",
                        "/css/**",
                        "/js/**",
                        "/vendor/**",
                        "/webjars/**",
                        "/actuator/**",
                        "/sys/**",
                        "/payment/**",
                        "/membership/ajax-search/**").permitAll()
                .antMatchers("/**").authenticated().and()

                .addFilterBefore(captchaFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?time=expired")
                .maximumSessions(10)
                .expiredUrl("/login?time=expired").and().and()

                .formLogin()
                .loginPage("/login")
                .permitAll()
                .failureUrl("/login-error")
                .defaultSuccessUrl("/index")
                .usernameParameter("username")
                .passwordParameter("password")
                .and()

                .authenticationProvider(authenticationProvider())
                .logout()
                .permitAll()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .and()

                .rememberMe()
                .key("ringmecms")
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(86400)
                .and()

                .csrf().disable()
                .headers().disable()
                .exceptionHandling().accessDeniedPage("/403");

        return http.build();
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken(); // Sử dụng header để truyền session id
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    public FilterRegistrationBean<CustomFilter> customFilterRegistration() {
        FilterRegistrationBean<CustomFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(customFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public CustomFilter customFilter() {
        return new CustomFilter();
    }

    @Bean
    public CaptchaFilter captchaFilter() {
        return new CaptchaFilter();
    }
}

