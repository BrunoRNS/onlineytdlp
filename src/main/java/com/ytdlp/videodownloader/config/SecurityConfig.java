package com.ytdlp.videodownloader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Returns a {@link SecurityFilterChain} that allows all requests, and disables both CSRF and HTTP Basic authentication.
     *
     * @param http the {@link HttpSecurity} object to configure
     * @return a configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while building the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            .csrf(csrf -> csrf.disable())

            .formLogin(form -> form.disable())

            .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
