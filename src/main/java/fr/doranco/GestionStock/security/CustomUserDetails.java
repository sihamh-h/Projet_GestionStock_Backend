package fr.doranco.GestionStock.security;

import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.model.entity.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
public class CustomUserDetails  implements UserDetails {

    private final Long id;
    private final String firstname;
    private final String lastname;
    private final  String email;
    private final  String password;
    private final Set<Role> roles;
    private final  boolean enabled;
    private final boolean mustChangePassword;
    private final boolean deleted;
    private final String imageUrl;;


    public CustomUserDetails(Long id, String firstname, String lastname, String email, String password, Set<Role> roles, boolean enabled, boolean mustChangePassword, boolean deleted, String imageUrl) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.enabled = enabled;
        this.mustChangePassword = mustChangePassword;
        this.deleted = deleted;
        this.imageUrl = imageUrl;
    }

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.roles = user.getRoles();
        this.enabled = user.isEnabled();
        this.mustChangePassword = user.isMustChangePassword();
        this.deleted = user.isDeleted();
        this.imageUrl = user.getImageUrl();
    }

    @Override

    public Collection<? extends GrantedAuthority> getAuthorities() {
       // return authorities;
        //return List.of(new SimpleGrantedAuthority(role.name()));
        /*return roles.stream()
                .map(role -> new SimpleGrantedAuthority( role.getName().name()))
                .toList();*/

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            // Ajoute le rôle avec le préfixe ROLE_ (requis par hasRole())
            authorities.add(new SimpleGrantedAuthority( role.getName().name()));

            // Ajoute toutes les permissions du rôle
            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName().name()))
            );
        }

        return authorities;

    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled && !deleted;
    }


}
