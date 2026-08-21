package com.dynamicstay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    InMemoryUserDetailsManager userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${security.users.manager.username}") String managerUsername,
            @Value("${security.users.manager.password}") String managerPassword,
            @Value("${security.users.admin.username}") String adminUsername,
            @Value("${security.users.admin.password}") String adminPassword) {
        UserDetails manager = User.withUsername(managerUsername)
                .password(passwordEncoder.encode(managerPassword))
                .roles("MANAGER")
                .build();
        UserDetails admin = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN", "MANAGER")
                .build();
        return new InMemoryUserDetailsManager(manager, admin);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/bookings/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/bookings/**", "/api/pricing/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/api/rooms/**", "/api/occupancy/**").hasAnyRole("MANAGER", "ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(basic -> {});
        return http.build();
    }
}