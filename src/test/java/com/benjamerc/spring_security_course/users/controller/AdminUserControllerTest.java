package com.benjamerc.spring_security_course.users.controller;

import com.benjamerc.spring_security_course.authentication.exception.UsernameAlreadyExistsException;
import com.benjamerc.spring_security_course.security.core.AccessTokenService;
import com.benjamerc.spring_security_course.shared.advice.GlobalExceptionHandler;
import com.benjamerc.spring_security_course.shared.builder.ApiErrorBuilder;
import com.benjamerc.spring_security_course.shared.dto.pagination.CustomPage;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.dto.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.AdminUserResponse;
import com.benjamerc.spring_security_course.users.dto.response.AdminUserSummaryResponse;
import com.benjamerc.spring_security_course.users.exception.UserNotFoundException;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.service.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, ApiErrorBuilder.class})
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    void shouldReturn200AndAllUsers() throws Exception {

        User admin = UserTestDataProvider.admin(1L);
        User user = UserTestDataProvider.user(2L);

        CustomPage<AdminUserSummaryResponse> customPage =
                new CustomPage<>(
                        List.of(new AdminUserSummaryResponse(user.getId(), user.getUsername(), user.getName(), user.getRole())),
                        0, 20, 1, 1, true
                );

        when(adminUserService.getAllUsers(any(Pageable.class))).thenReturn(customPage);

        mockMvc.perform(get("/api/admin/users")
                        .with(user(admin.getUsername())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(user.getId()))
                .andExpect(jsonPath("$.content[0].username").value(user.getUsername()))
                .andExpect(jsonPath("$.content[0].name").value(user.getName()))
                .andExpect(jsonPath("$.content[0].role").value(user.getRole().toString()))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.lastPage").value(true));

        verify(adminUserService).getAllUsers(any(Pageable.class));
    }

    @Test
    void shouldReturn200AndUserById() throws Exception {

        User admin = UserTestDataProvider.admin(1L);
        User user = UserTestDataProvider.user(2L);

        AdminUserResponse adminUserResponse =
                new AdminUserResponse(user.getId(), user.getUsername(), user.getName(), user.getRole());

        when(adminUserService.getUserById(user.getId())).thenReturn(adminUserResponse);

        mockMvc.perform(get("/api/admin/users/{id}", user.getId())
                        .with(user(admin.getUsername())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adminUserResponse.id()))
                .andExpect(jsonPath("$.username").value(adminUserResponse.username()))
                .andExpect(jsonPath("$.name").value(adminUserResponse.name()))
                .andExpect(jsonPath("$.role").value(adminUserResponse.role().toString()));

        verify(adminUserService).getUserById(user.getId());
    }

    @Test
    void shouldThrow404WhenGetUserByIdCalledWithNonexistingId() throws Exception {

        User admin = UserTestDataProvider.admin(1L);

        long nonExistingId = 99L;

        when(adminUserService.getUserById(nonExistingId))
                .thenThrow(new UserNotFoundException("User not found with id: " + nonExistingId));

        mockMvc.perform(get("/api/admin/users/{id}", nonExistingId)
                        .with(user(admin.getUsername())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistingId));

        verify(adminUserService).getUserById(nonExistingId);
    }

    @Test
    void shouldReturn200AndUpdateUserById() throws Exception {

        User admin = UserTestDataProvider.admin(1L);
        User user = UserTestDataProvider.user(2L);

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        AdminUserResponse updateResponse =
                new AdminUserResponse(user.getId(), updateRequest.username(), user.getName(), user.getRole());

        when(adminUserService.partialUpdate(user.getId(), updateRequest)).thenReturn(updateResponse);

        performPatch("/api/admin/users/{id}", user.getId(), updateRequest, admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updateResponse.id()))
                .andExpect(jsonPath("$.username").value(updateResponse.username()))
                .andExpect(jsonPath("$.name").value(updateResponse.name()))
                .andExpect(jsonPath("$.role").value(updateResponse.role().toString()));

        verify(adminUserService).partialUpdate(user.getId(), updateRequest);
    }

    @Test
    void shouldThrow400WhenPartialUpdateCalledWithExistentUsername() throws Exception {

        User admin = UserTestDataProvider.admin(1L);
        User user = UserTestDataProvider.user(2L);

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        when(adminUserService.partialUpdate(user.getId(), updateRequest))
                .thenThrow(new UsernameAlreadyExistsException("Username already exists"));

        performPatch("/api/admin/users/{id}", user.getId(), updateRequest, admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));

        verify(adminUserService).partialUpdate(user.getId(), updateRequest);
    }

    @Test
    void shouldThrow404WhenPartialUpdateCalledWithNonexistentId() throws Exception {

        User admin = UserTestDataProvider.admin(1L);

        long nonExistentId = 99L;

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        when(adminUserService.partialUpdate(nonExistentId, updateRequest))
                .thenThrow(new UserNotFoundException("User not found with id: " + nonExistentId));

        performPatch("/api/admin/users/{id}", nonExistentId, updateRequest, admin)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentId));

        verify(adminUserService).partialUpdate(nonExistentId, updateRequest);
    }

    @Test
    void shouldReturn204AndDeleteUserAccount() throws Exception {

        User admin = UserTestDataProvider.admin(1L);
        User user = UserTestDataProvider.user(2L);

        doNothing().when(adminUserService).deleteUser(user.getId());

        performDelete("/api/admin/users/{id}", user.getId(), admin)
                .andExpect(status().isNoContent())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEmpty());

        verify(adminUserService).deleteUser(user.getId());
    }

    @Test
    void shouldThrow404WhenDeleteUserCalledWithNonexistentId() throws Exception {

        User admin = UserTestDataProvider.admin(1L);

        long nonExistentId = 99L;

        doThrow(new UserNotFoundException("User not found with id: " + nonExistentId))
                .when(adminUserService).deleteUser(nonExistentId);

        performDelete("/api/admin/users/{id}", nonExistentId, admin)
                .andExpect(status().isNotFound());

        verify(adminUserService).deleteUser(nonExistentId);
    }

    @Test
    void shouldReturn204AndLogoutAllUserSessions() throws Exception {

        User admin = UserTestDataProvider.admin(1L);
        User user = UserTestDataProvider.user(2L);

        doNothing().when(adminUserService).logoutAll(user.getId());

        performPost("/api/admin/users/{id}/logout-all", user.getId(), admin)
                .andExpect(status().isNoContent())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEmpty());

        verify(adminUserService).logoutAll(user.getId());
    }

    @Test
    void shouldThrow404WhenLogoutAllCalledWithNonexistentId() throws Exception {

        User admin = UserTestDataProvider.admin(1L);

        long nonExistentId = 99L;

        doThrow(new UserNotFoundException("User not found with id: " + nonExistentId))
                .when(adminUserService).logoutAll(nonExistentId);

        performPost("/api/admin/users/{id}/logout-all", nonExistentId, admin)
                .andExpect(status().isNotFound());

        verify(adminUserService).logoutAll(nonExistentId);
    }

    private ResultActions performPatch(String url, long id, Object body, User admin) throws Exception {

        return mockMvc.perform(patch(url, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(user(admin.getUsername())));
    }

    private ResultActions performDelete(String url, long id, User admin) throws Exception {

        return mockMvc.perform(delete(url, id)
                .with(user(admin.getUsername())));
    }

    private ResultActions performPost(String url, long id, User admin) throws Exception {

        return mockMvc.perform(post(url, id)
                .with(user(admin.getUsername())));
    }
}
