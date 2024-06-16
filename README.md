# SpringBoot 3


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
        return http.build();
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

### @EnableWebSecurity
A anotação @EnableWebSecurity é usada para habilitar a configuração de segurança no Spring Boot.
No Spring Security 5.6, podemos habilitar a segurança baseada em anotações usando a anotação @EnableMethodSecurity em qualquer instância @Configuration.

## Usuario no banco de dados
Modelo de usuário:

```dtd
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "tab_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 50, nullable = false)
    private String name;
    @Column(length = 20, nullable = false)
    private String username;
    @Column(length = 100, nullable = false)
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tab_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_id")
    private List<String> roles = new ArrayList<>();

}
```

## Repositorio:

Implementação do repositório:

```dtd
import me.dio.security.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT e FROM User e JOIN FETCH e.roles WHERE e.username= (:username)")
    public User findByUsername(@Param("username") String username);
}

```

## SecurityDatabaseService

Implementação do SecurityDatabaseService:

```dtd
@Service
public class SecurityDatabaseService  implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) {
        User userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }
        Set<GrantedAuthority> authorities = new HashSet<>();
        userEntity.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        });
        UserDetails user = new org.springframework.security.core.userdetails.User(userEntity.getUsername(),
                userEntity.getPassword(),
                authorities);
        return user;
    }
}
```

## Configurações

É necessário injetar o SecurityDatabaseService na classe de configuração da autenticação

```dtd
    @Autowired
        private SecurityDatabaseService securityDatabaseService;
    @Autowired
    public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(securityDatabaseService).passwordEncoder(NoOpPasswordEncoder.getInstance());
    }
```

*Para usar http basic subistitua o login form por httpBasic(withDefaults())
