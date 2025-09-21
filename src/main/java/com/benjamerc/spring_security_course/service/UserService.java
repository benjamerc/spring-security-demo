package com.benjamerc.spring_security_course.service;

import com.benjamerc.spring_security_course.domain.dto.user.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserProfileResponse;
import com.benjamerc.spring_security_course.security.CustomUserDetails;

public interface UserService {

    UserProfileResponse userProfile(CustomUserDetails userDetails);

    UserPartialUpdateResponse updateProfile(CustomUserDetails userDetails, UserPartialUpdateRequest request);

    void deleteAccount(CustomUserDetails userDetails);

    void logoutAll(CustomUserDetails userDetails);
}
