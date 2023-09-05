package cl.pruebasermaluc.security;


import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@AllArgsConstructor
public class WebSecurityService {

    private final UserDetailsService userDetailsService;
    private final JWTAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {

        JWTAuthenticationFilter jwtAuthenticacionFilter = new JWTAuthenticationFilter();
        jwtAuthenticacionFilter.setAuthenticationManager(authManager);
        jwtAuthenticacionFilter.setFilterProcessesUrl("/login");

        return http
                .csrf().disable()
                .authorizeRequests()
                // Rutas públicas que no requieren autenticación
                .antMatchers("/public/**").permitAll()
                // Rutas protegidas por roles
                .antMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "MODERATOR")
                .antMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "MODERATOR", "USER") // Agregar si hay operaciones POST protegidas
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(jwtAuthenticacionFilter)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

