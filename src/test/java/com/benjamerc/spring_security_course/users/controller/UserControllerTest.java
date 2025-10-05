package com.benjamerc.spring_security_course.users.controller;

import com.benjamerc.spring_security_course.authentication.exception.UsernameAlreadyExistsException;
import com.benjamerc.spring_security_course.security.core.AccessTokenService;
import com.benjamerc.spring_security_course.security.core.CustomUserDetails;
import com.benjamerc.spring_security_course.shared.advice.GlobalExceptionHandler;
import com.benjamerc.spring_security_course.shared.builder.ApiErrorBuilder;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.users.dto.response.UserProfileResponse;
import com.benjamerc.spring_security_course.users.exception.UserNotFoundException;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import com.benjamerc.spring_security_course.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, ApiErrorBuilder.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldReturn200AndUserProfile() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        UserProfileResponse profileResponse =
                new UserProfileResponse(userDetails.getUsername(), userDetails.getUser().getName());

        when(userService.userProfile(userDetails)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/user/me")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(profileResponse.username()))
                .andExpect(jsonPath("$.name").value(profileResponse.name()));

        verify(userService).userProfile(userDetails);
    }

    @Test
    void shouldThrow404WhenUserProfileCalledWithNonexistentUser() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        when(userService.userProfile(any(CustomUserDetails.class)))
                .thenThrow(new UserNotFoundException("User not found with id: " + userDetails.getUser().getId()));

        mockMvc.perform(get("/api/user/me")
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());

        verify(userService).userProfile(any(CustomUserDetails.class));
    }

    @Test
    void shouldReturn200AndUpdatedUserProfile() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        UserPartialUpdateRequest updateRequest =
                UserTestDataProvider.userPartialUpdateRequest();

        UserPartialUpdateResponse updateResponse =
                new UserPartialUpdateResponse(userDetails.getUsername(), updateRequest.name());

        when(userService.updateProfile(any(CustomUserDetails.class), any(UserPartialUpdateRequest.class))).thenReturn(updateResponse);

        performPatch("/api/user/me", updateRequest, userDetails)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(updateResponse.username()))
                .andExpect(jsonPath("$.name").value(updateResponse.name()));

        verify(userService).updateProfile(any(CustomUserDetails.class), any(UserPartialUpdateRequest.class));
    }

    @Test
    void shouldThrow404WhenUpdateProfileCalledWithNonexistentUser() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(99L);

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest();

        when(userService.updateProfile(any(CustomUserDetails.class), any(UserPartialUpdateRequest.class)))
                .thenThrow(new UserNotFoundException("User not found with id: " + userDetails.getUser().getId()));

        performPatch("/api/user/me", updateRequest, userDetails)
                .andExpect(status().isNotFound());

        verify(userService).updateProfile(any(CustomUserDetails.class), any(UserPartialUpdateRequest.class));
    }

    @Test
    void shouldThrow400WhenUpdateProfileCalledWithInvalidDto() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest(null, "");

        performPatch("/api/user/me", updateRequest, userDetails)
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(CustomUserDetails.class), any(UserPartialUpdateRequest.class));
    }

    @Test
    void shouldThrow400WhenUpdateProfileCalledWithExistentUsername() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        UserPartialUpdateRequest updateRequest = UserTestDataProvider.userPartialUpdateRequest("existent@email.com", null);

        when(userService.updateProfile(any(CustomUserDetails.class), any(UserPartialUpdateRequest.class)))
                .thenThrow(new UsernameAlreadyExistsException("Username already exists"));

        performPatch("/api/user/me", updateRequest, userDetails)
                .andExpect(status().isBadRequest());

        verify(userService).updateProfile(any(CustomUserDetails.class), any(UserPartialUpdateRequest.class));
    }

    @Test
    void shouldReturn204AndDeleteUserAccount() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        performDelete("/api/user/me", userDetails)
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldThrow404WhenDeleteAccountCalledWithNonexistentUser() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(99L);

        doThrow(new UserNotFoundException("User not found"))
                .when(userService).deleteAccount(any(CustomUserDetails.class));

        performDelete("/api/user/me", userDetails)
                .andExpect(status().isNotFound());

        verify(userService).deleteAccount(any(CustomUserDetails.class));
    }

    @Test
    void shouldReturn204AndLogoutAllUserSessions() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(1L);

        performPost("/api/user/me/logout-all", userDetails)
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldThrow404WhenLogoutAllCalledWithNonexistentUser() throws Exception {

        CustomUserDetails userDetails = UserTestDataProvider.testUser(99L);

        doThrow(new UserNotFoundException("User not found"))
                .when(userService).logoutAll(any(CustomUserDetails.class));

        performPost("/api/user/me/logout-all", userDetails)
                .andExpect(status().isNotFound());

        verify(userService).logoutAll(any(CustomUserDetails.class));
    }

    private ResultActions performPatch(String url, Object body, CustomUserDetails userDetails) throws Exception {

        return mockMvc.perform(patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(user(userDetails))
                .with(csrf()));
    }

    private ResultActions performPost(String url, CustomUserDetails userDetails) throws Exception {

        return mockMvc.perform(post(url)
                .with(user(userDetails))
                .with(csrf()));
    }

    private ResultActions performDelete(String url, CustomUserDetails userDetails) throws Exception {

        return mockMvc.perform(delete(url)
                .with(user(userDetails))
                .with(csrf()));
    }
}
