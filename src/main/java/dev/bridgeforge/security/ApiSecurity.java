package dev.bridgeforge.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ApiSecurity {
    @Bean
    UserDetailsService users(Environment env) {
        String password = env.getProperty("BRIDGEFORGE_ADMIN_PASSWORD", "");
        if (password.length() < 16)
            throw new IllegalStateException("Set BRIDGEFORGE_ADMIN_PASSWORD to at least 16 characters");
        var encoder = new BCryptPasswordEncoder();
        var admin = User.withUsername(env.getProperty("BRIDGEFORGE_ADMIN_USER", "portfolio-admin"))
            .password(encoder.encode(password)).roles("ADMIN", "READER").build();
        String readerPassword = env.getProperty("BRIDGEFORGE_READER_PASSWORD", "");
        if (readerPassword.isBlank()) return new InMemoryUserDetailsManager(admin);
        if (readerPassword.length() < 16)
            throw new IllegalStateException("BRIDGEFORGE_READER_PASSWORD must be at least 16 characters");
        var reader = User.withUsername(env.getProperty("BRIDGEFORGE_READER_USER", "portfolio-reader"))
            .password(encoder.encode(readerPassword)).roles("READER").build();
        return new InMemoryUserDetailsManager(admin, reader);
    }

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        // HTTP Basic is only for the local CLI / stateless API demo. Always use TLS.
        return http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/customers/**").hasAnyRole("READER", "ADMIN")
                .requestMatchers("/api/v1/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .httpBasic(Customizer.withDefaults()).build();
    }
}
