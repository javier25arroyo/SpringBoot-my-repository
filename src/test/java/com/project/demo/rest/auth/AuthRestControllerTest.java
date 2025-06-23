package com.project.demo.rest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.LoginResponse;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    // @MockBean // Removed to allow the actual PasswordEncoder bean to be used
    // private PasswordEncoder passwordEncoder;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Role userRole;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test");
        user.setLastname("User");
        user.setEmail("test@example.com");
        user.setPassword("password");

        userRole = new Role();
        userRole.setName(RoleEnum.USER);
        userRole.setId(1);

        user.setRole(userRole); // Set role in setup to avoid NPE during request serialization if it occurs
    }

    @Test
    void signup_success() throws Exception {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        // The actual PasswordEncoder bean will be used, no need to mock its encode method here.
        when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // Ensure the user being "saved" has its role, which it should from controller logic
            // Forcing it here for the mock's returned object just in case.
            if (userToSave.getRole() == null) userToSave.setRole(userRole);
            userToSave.setId(1L); // Simulate saving and getting an ID
            return userToSave;
        });

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void signup_emailAlreadyInUse() throws Exception {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already in use"));
    }

    @Test
    void signup_roleNotFound() throws Exception {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        // The actual PasswordEncoder bean will be used.
        when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Role not found"));
    }

    @Test
    void login_success() throws Exception {
        User authenticatedUser = new User();
        authenticatedUser.setEmail(user.getEmail());
        authenticatedUser.setId(1L);
        authenticatedUser.setRole(userRole); // Ensure role is set for serialization

        when(authenticationService.authenticate(any(User.class))).thenReturn(authenticatedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(authenticatedUser));


        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600000L))
                .andExpect(jsonPath("$.authUser.email").value(user.getEmail()));
    }

    @Test
    void login_invalidCredentials() throws Exception {
        when(authenticationService.authenticate(any(User.class))).thenThrow(new RuntimeException("Authentication failed"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isInternalServerError()); // Or whatever status the GlobalExceptionHandler returns
    }
}
