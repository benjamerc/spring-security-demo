package com.benjamerc.spring_security_course.users.service;

import com.benjamerc.spring_security_course.authentication.exception.UsernameAlreadyExistsException;
import com.benjamerc.spring_security_course.authentication.security.RefreshTokenService;
import com.benjamerc.spring_security_course.security.core.Role;
import com.benjamerc.spring_security_course.users.UserTestDataProvider;
import com.benjamerc.spring_security_course.users.dto.request.AdminUserUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.AdminUserResponse;
import com.benjamerc.spring_security_course.users.dto.response.AdminUserSummaryResponse;
import com.benjamerc.spring_security_course.users.exception.UserNotFoundException;
import com.benjamerc.spring_security_course.users.mapper.AdminUserMapper;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminUserMapper adminUserMapper;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Test
    void shouldReturnPagedUsers() {

        ReflectionTestUtils.setField(adminUserService, "maxPageSize", 50);

        User user1 = UserTestDataProvider.user(1L);
        User user2 = UserTestDataProvider.user(2L);

        List<User> users = List.of(user1, user2);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 20), users.size());

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(adminUserMapper.toAdminUserSummaryResponse(user1)).thenReturn(
                new AdminUserSummaryResponse(user1.getId(), user1.getUsername(), user1.getName(), user1.getRole()));
        when(adminUserMapper.toAdminUserSummaryResponse(user2)).thenReturn(
                new AdminUserSummaryResponse(user2.getId(), user2.getUsername(), user2.getName(), user2.getRole()));

        Page<AdminUserSummaryResponse> result = adminUserService.getAllUsers(PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(userRepository).findAll(any(Pageable.class));
        verify(adminUserMapper).toAdminUserSummaryResponse(user1);
        verify(adminUserMapper).toAdminUserSummaryResponse(user2);
    }

    @Test
    void shouldRespectMaxPageSizeWhenCreatingSafePageable() {

        int maxPageSizeValue = 50;

        ReflectionTestUtils.setField(adminUserService, "maxPageSize", maxPageSizeValue);

        User user1 = UserTestDataProvider.user(1L);

        List<User> users = List.of(user1);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, maxPageSizeValue), users.size());

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(adminUserMapper.toAdminUserSummaryResponse(user1)).thenReturn(
                new AdminUserSummaryResponse(user1.getId(), user1.getUsername(), user1.getName(), user1.getRole()));

        Page<AdminUserSummaryResponse> result = adminUserService.getAllUsers(PageRequest.of(0, 100));

        assertThat(result.getPageable().getPageSize()).isEqualTo(maxPageSizeValue);

        verify(userRepository).findAll(any(Pageable.class));
        verify(adminUserMapper).toAdminUserSummaryResponse(user1);
    }

    @Test
    void shouldReturnUserById() {

        User user = UserTestDataProvider.user(1L);

        AdminUserResponse response =
                new AdminUserResponse(user.getId(), user.getUsername(), user.getName(), user.getRole());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(adminUserMapper.toAdminUserResponse(user)).thenReturn(response);

        AdminUserResponse result = adminUserService.getUserById(user.getId());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(response.id());
        assertThat(result.username()).isEqualTo(response.username());
        assertThat(result.name()).isEqualTo(response.name());
        assertThat(result.role()).isEqualTo(response.role());

        verify(userRepository).findById(user.getId());
        verify(adminUserMapper).toAdminUserResponse(user);
    }

    @Test
    void shouldThrowUserNotFoundWhenGetUserByIdCalledWithNonexistingUserId() {

        long nonExistingUserId = 99L;

        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserById(nonExistingUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + nonExistingUserId);

        verify(userRepository).findById(nonExistingUserId);
    }

    @Test
    void shouldPartialUpdateUserById() {

        User user = UserTestDataProvider.user(1L);

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        User updatedUser = UserTestDataProvider.user(user.getId());
        updatedUser.setUsername(updateRequest.username());

        AdminUserResponse response =
                new AdminUserResponse(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getName(), updatedUser.getRole());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(adminUserMapper.toAdminUserResponse(updatedUser)).thenReturn(response);

        AdminUserResponse result = adminUserService.partialUpdate(user.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(response.id());
        assertThat(result.username()).isEqualTo(response.username());
        assertThat(result.name()).isEqualTo(response.name());
        assertThat(result.role()).isEqualTo(response.role());

         verify(userRepository).findById(user.getId());
         verify(userRepository).save(any(User.class));
         verify(adminUserMapper).toAdminUserResponse(updatedUser);
    }

    @Test
    void shouldNotUpdateValueInPartialUpdateWhenRequestContainsBlankValue() {

        User user = UserTestDataProvider.user(1L);

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest("", "", null);

        AdminUserResponse response =
                new AdminUserResponse(user.getId(), user.getUsername(), user.getName(), user.getRole());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminUserMapper.toAdminUserResponse(user)).thenReturn(response);

        AdminUserResponse result = adminUserService.partialUpdate(user.getId(), updateRequest);

        assertThat(result.username()).isEqualTo(user.getUsername());
        assertThat(result.name()).isEqualTo(user.getName());
        assertThat(result.role()).isEqualTo(user.getRole());

        verify(userRepository).findById(user.getId());
        verify(userRepository).save(any(User.class));
        verify(adminUserMapper).toAdminUserResponse(user);
    }

    @Test
    void shouldNotUpdateRoleWhenRequestContainsAdminRole() {

        User user = UserTestDataProvider.user(1L);

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest("", "", Role.ADMIN);

        AdminUserResponse response =
                new AdminUserResponse(user.getId(), user.getUsername(), user.getName(), user.getRole());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminUserMapper.toAdminUserResponse(user)).thenReturn(response);

        AdminUserResponse result = adminUserService.partialUpdate(user.getId(), updateRequest);

        assertThat(result.role()).isEqualTo(user.getRole());

        verify(userRepository).findById(user.getId());
        verify(userRepository).save(any(User.class));
        verify(adminUserMapper).toAdminUserResponse(user);
    }

    @Test
    void shouldThrowUsernameAlreadyExistsWhenPartialUpdateCalledWithExistentUsername() {

        User user = UserTestDataProvider.user(1L);

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        when(userRepository.existsByUsername(updateRequest.username()))
                .thenThrow(new UsernameAlreadyExistsException("Username already exists"));

        assertThatThrownBy(() -> adminUserService.partialUpdate(user.getId(), updateRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already exists");

        verify(userRepository).existsByUsername(updateRequest.username());
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowUserNotFoundWhenPartialUpdateCalledWithNonexistingUserId() {

        long nonExistingUserId = 99L;

        AdminUserUpdateRequest updateRequest = UserTestDataProvider.adminUserUpdateRequest();

        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.partialUpdate(nonExistingUserId, updateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + nonExistingUserId);

        verify(userRepository).findById(nonExistingUserId);
    }

    @Test
    void shouldDeleteUserById() {

        User user = UserTestDataProvider.user(1L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(eq(user));

        adminUserService.deleteUser(user.getId());

        verify(userRepository).findById(user.getId());
        verify(userRepository).delete(eq(user));
    }

    @Test
    void shouldThrowUserNotFoundWhenDeleteUserCalledWithNonexistingUserId() {

        long nonExistingUserId = 99L;

        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.deleteUser(nonExistingUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + nonExistingUserId);

        verify(userRepository).findById(nonExistingUserId);
    }

    @Test
    void shouldLogoutAllUserSessionsById() {

        User user = UserTestDataProvider.user(1L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenService).revokeAllTokensForUser(eq(user));

        adminUserService.logoutAll(user.getId());

        verify(userRepository).findById(user.getId());
        verify(refreshTokenService).revokeAllTokensForUser(eq(user));
    }

    @Test
    void shouldThrowUserNotFoundWhenLogoutAllCalledWithNonexistingUserId() {

        long nonExistingUserId = 99L;

        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.logoutAll(nonExistingUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + nonExistingUserId);

        verify(userRepository).findById(nonExistingUserId);
    }
}
