package com.benjamerc.spring_security_course.service;

import com.benjamerc.spring_security_course.domain.dto.user.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserProfileResponse;
import org.springframework.security.core.Authentication;

public interface UserService {

    UserProfileResponse userProfile(Authentication authentication);

    UserPartialUpdateResponse updateProfile(Authentication authentication, UserPartialUpdateRequest request);

    void deleteAccount(Authentication authentication);
}
