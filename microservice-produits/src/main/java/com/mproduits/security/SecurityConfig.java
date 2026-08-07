package com.mproduits.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Fondation securite de microservice-produits, calquee sur celle de
 * microservice-administration : JWT stateless, tout endpoint authentifie par
 * defaut. Le CORS reste centralise au niveau du gateway (voir CLAUDE.md) —
 * produits n'est jamais appele directement par le navigateur, donc aucune
 * configuration CORS n'est necessaire ici.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private LicenceCheckFilter licenceCheckFilter;

    @Autowired
    private TenantScopeFilter tenantScopeFilter;

    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(licenceCheckFilter, JwtAuthFilter.class)
                .addFilterAfter(tenantScopeFilter, LicenceCheckFilter.class);

        return http.build();
    }
}
