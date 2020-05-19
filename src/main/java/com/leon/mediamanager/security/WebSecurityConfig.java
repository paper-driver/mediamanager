package com.leon.mediamanager.security;

import org.apache.catalina.filters.RequestDumperFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.leon.mediamanager.security.jwt.AuthEntryPointJwt;
import com.leon.mediamanager.security.jwt.AuthTokenFilter;
import com.leon.mediamanager.security.services.UserDetailsServiceImpl;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(2)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

//    @Autowired
//    private CORSFilter corsFilter;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Profile("!cloud")
    @Bean
    RequestDumperFilter requestDumperFilter() {
        return new RequestDumperFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/protected/**").permitAll()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/favicon.ico").permitAll()
                .anyRequest().authenticated().and()
                .requestMatchers().antMatchers( "/api/public/**", "/api/auth/**", "/api/protected/**");

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Creates a request matcher which only matches requests for a specific local port and path (using an
     * {@link AntPathRequestMatcher} for the path part).
     *
     * @param   port         the port to match
     * @param   pathPattern  the pattern for the path.
     *
     * @return  the new request matcher.
     */
    private RequestMatcher forPortAndPath(final int port, final String pathPattern) {
        return new AndRequestMatcher(forPort(port), new AntPathRequestMatcher(pathPattern));
    }

    /**
     * Creates a request matcher which only matches requests for a specific local port, path and request method (using
     * an {@link AntPathRequestMatcher} for the path part).
     *
     * @param   port         the port to match
     * @param   pathPattern  the pattern for the path.
     * @param   method       the HttpMethod to match. Requests for other methods will not be matched.
     *
     * @return  the new request matcher.
     */
    private RequestMatcher forPortAndPath(final int port,  final HttpMethod method,
                                          final String pathPattern) {
        return new AndRequestMatcher(forPort(port), new AntPathRequestMatcher(pathPattern, method.name()));
    }

    /**
     * A request matcher which matches just a port.
     *
     * @param   port  the port to match.
     *
     * @return  the new matcher.
     */
    private RequestMatcher forPort(final int port) {
        return (HttpServletRequest request) -> { return port == request.getLocalPort(); };
    }




//    @Override
//    public void configure(WebSecurity web) throws Exception {
////        web.ignoring().regexMatchers("/api/roleconfirmation?token=([0-9]+$)|([0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12})");
//        web.ignoring().antMatchers("api/public/**");
//    }
}
