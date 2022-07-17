package org.pjp.rosta.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pjp.rosta.ui.view.login.LoginView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.vaadin.flow.spring.security.VaadinWebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

    public static class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

        public CustomLoginFailureHandler(String defaultFailureUrl) {
            super(defaultFailureUrl);
        }

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
            LOGGER.debug("login failure for request = {}", request);
            super.onAuthenticationFailure(request, response, exception);
        }
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Delegating the responsibility of general configurations
        // of http security to the super class. It is configuring
        // the followings: Vaadin's CSRF protection by ignoring
        // framework's internal requests, default request cache,
        // ignoring public views annotated with @AnonymousAllowed,
        // restricting access to other views/endpoints, and enabling
        // ViewAccessChecker authorization.

        super.configure(http);

        // You can add any possible extra configurations of your own
        // here (the following is just an example):
        // http.rememberMe().alwaysRemember(false);

        // https://stackoverflow.com/questions/72851711/vaadin-loginform-failure-handler-breaks-loginerror-url/72859368
        //http.formLogin().failureHandler(new CustomLoginFailureHandler("/login?error"));

        // This is important to register your login view to the
        // view access checker mechanism:
        setLoginView(http, LoginView.class);
    }

    /**
     * Allows access to static resources, bypassing Spring security.
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // Configure your static resources with public access here:
        web.ignoring().antMatchers(
                "/images/**",
                "/icons/**"
        );

        // Delegating the ignoring configuration for Vaadin's
        // related static resources to the super class:
        super.configure(web);
    }

}