package com.studentplansystem.studyplangym.config;

import com.studentplansystem.studyplangym.util.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "img-src 'self' data:; " +
                                                "font-src 'self'; " +
                                                "connect-src 'self' http://localhost:8080 http://localhost:5173 https://studyplanfrontend.vercel.app https://*.vercel.app; " +
                                                "frame-ancestors 'none'; " +
                                                "base-uri 'self'; " +
                                                "form-action 'self'"
                                )
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/password-reset/request").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/password-reset/requests").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/password-reset/complete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/password-reset/deny/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/maintenance/status").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/maintenance/status").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/audit-logs").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/config/categories").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/config/school-years").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/api/config/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/studyplans").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/studyplans/export").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/studyplans/edit-requests").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/studyplans/approve-edit/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/studyplans/deny-edit/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/studyplans/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/studyplans").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/studyplans/student/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/studyplans/request-edit/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.PUT, "/api/studyplans/update/**").hasRole("STUDENT")

                        .requestMatchers(HttpMethod.POST, "/api/change-password").hasAnyRole("STUDENT", "ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}