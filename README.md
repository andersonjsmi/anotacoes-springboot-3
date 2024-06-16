#SpringBoot 3


## Autenticação Simples
A classe WebConfigurerAdapter foi removida no Spring Boot 3, conforme parte da evolução e simplificação da configuração de segurança do Spring Security. Agora, as configurações de segurança são feitas diretamente na classe de configuração, utilizando a classe SecurityFilterChain.


Implementando a configuração:

```dtd
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public UserDetailsService userDetailsService() {
        var userDetailsManager = new InMemoryUserDetailsManager();

        var user = User
                .withDefaultPasswordEncoder() // metodo obsoleto, usado apenas para teste
                .username("user")
                .password("pass")
                .roles("USERS")
                .build();

        userDetailsManager.createUser(user);

        return userDetailsManager;
    }
    
}

````

Testando a autorização: 

```dtd
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    @GetMapping
    public String welcome () {
        return "Welcome!";
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('managers','users')")
    public String users () {
        return "Authorized user!";
    }

    @GetMapping("/managers")
    @PreAuthorize("hashRole('managers')")
    public String managers () {
        return "Authorized Manager";
    }

}

```

## Configure Adapter

```dtd
@Configuration
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

```

###@EnableWebSecurity
A anotação @EnableWebSecurity é usada para habilitar a configuração de segurança no Spring Boot.
No Spring Security 5.6, podemos habilitar a segurança baseada em anotações usando a anotação @EnableMethodSecurity em qualquer instância @Configuration.