package me.dio.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securitFilterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/managers").hasAnyRole("/MANAGERS")
                .requestMatchers("/users").hasAnyRole("USERS","MANAGERS")

        ).formLogin(form -> form
                .loginPage("/login")
                .permitAll()
        ).logout(
                //logout -> logout.permitAll()
                LogoutConfigurer::permitAll
        );
        return null;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var userDetailsManager = new InMemoryUserDetailsManager();
        var user = User
                .withDefaultPasswordEncoder()
                .username("user")
                .password("pass")
                .roles("USERS")
                .build();

        userDetailsManager.createUser(user);

        return userDetailsManager;
    }


}
