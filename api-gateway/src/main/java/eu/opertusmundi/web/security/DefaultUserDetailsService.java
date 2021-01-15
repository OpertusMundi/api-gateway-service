package eu.opertusmundi.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.web.model.security.User;

@Service
public class DefaultUserDetailsService implements CustomUserDetailsService {

    @Autowired
    private UserService   userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final AccountDto account = this.userService.findOneByUserName(username).orElse(null);

        if (account == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(account, account.getPassword());
    }

    @Override
    public UserDetails loadUserByUsername(String username, EnumAuthProvider provider) throws UsernameNotFoundException {
        final AccountDto account = this.userService.findOneByUserName(username, provider).orElse(null);

        if (account == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(account, account.getPassword());
    }

}

