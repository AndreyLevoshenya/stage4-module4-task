package com.mjc.school.auth;

import com.mjc.school.model.Author;
import com.mjc.school.model.Role;
import com.mjc.school.model.User;
import com.mjc.school.repository.AuthorRepository;
import com.mjc.school.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class Oauth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Oauth2LoginSuccessHandler.class);
    private static final String APPLICATION_JSON = "application/json";
    private static final String EMPTY = "";
    private static final String EMAIL = "email";
    private static final String GIVEN_NAME = "given_name";
    private static final String FAMILY_NAME = "family_name";

    @Value(value = "${frontend.url}")
    private String FRONTEND_URL;

    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final JwtService jwtService;

    @Autowired
    public Oauth2LoginSuccessHandler(UserRepository userRepository, JwtService jwtService, AuthorRepository authorRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authorRepository = authorRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = authToken.getPrincipal();

        String email = oAuth2User.getAttribute(EMAIL);
        String firstName = oAuth2User.getAttribute(GIVEN_NAME);
        String lastName = oAuth2User.getAttribute(FAMILY_NAME);

        User user = userRepository.findByUsername(email).orElseGet(() -> {
            User newUser = new User(null, firstName, lastName, email, EMPTY, Role.USER);
            LOGGER.info("Creating user with username {}", newUser.getUsername());
            User savedUser = userRepository.save(newUser);
            Author author = new Author(savedUser.getUsername(), LocalDateTime.now(), LocalDateTime.now(), List.of());
            author.setUser(savedUser);
            LOGGER.info("Creating author for user with username {}", savedUser.getUsername());
            authorRepository.save(author);
            return savedUser;
        });

        String jwt = jwtService.generateToken(user);
        response.setContentType(APPLICATION_JSON);
        response.sendRedirect(FRONTEND_URL + "/oauth2/callback?token=" + jwt);
    }

}