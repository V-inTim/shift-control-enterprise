package com.example.shift_control_enterprise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class ResourceServerConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/enterprises/time-point").hasAuthority("SCOPE_external.write")
                        .requestMatchers("/api/**").hasAuthority("SCOPE_external.read")
                        .requestMatchers("/enterprises/**").hasAuthority("SCOPE_enterprise")
                        .anyRequest()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())  // Более явная конфигурация JWT
                );

        return http.build();
    }

}
