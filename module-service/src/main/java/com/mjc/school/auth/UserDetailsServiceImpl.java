package com.mjc.school.auth;

import com.mjc.school.exception.NotFoundException;
import com.mjc.school.model.User;
import com.mjc.school.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.mjc.school.exception.ExceptionErrorCodes.USER_DOES_NOT_EXIST;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.debug("Loading user {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    LOGGER.error("User {} not found", username);
                    return new NotFoundException(String.format(USER_DOES_NOT_EXIST.getErrorMessage(), username));
                });

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities());
    }
}
