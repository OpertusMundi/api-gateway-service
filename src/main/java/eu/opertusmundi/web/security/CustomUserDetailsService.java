package eu.opertusmundi.web.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import eu.opertusmundi.common.model.EnumAuthProvider;

public interface CustomUserDetailsService extends UserDetailsService {

    UserDetails loadUserByUsername(String username, EnumAuthProvider provider) throws UsernameNotFoundException;

}
