package com.mjc.school.auth;

import com.mjc.school.model.Role;
import com.mjc.school.model.User;
import com.mjc.school.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Oauth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String APPLICATION_JSON = "application/json";
    private static final String EMPTY = "";
    private static final String EMAIL = "email";
    private static final String GIVEN_NAME = "given_name";
    private static final String FAMILY_NAME = "family_name";

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public Oauth2LoginSuccessHandler(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
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
            return userRepository.save(newUser);
        });

        String jwt = jwtService.generateToken(user);
        response.setContentType(APPLICATION_JSON);
        response.getWriter().write("{\"token\": \"" + jwt + "\"}");
        response.getWriter().flush();
    }

}
