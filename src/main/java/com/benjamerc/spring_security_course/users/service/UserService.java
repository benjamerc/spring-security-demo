package com.benjamerc.spring_security_course.users.service;

import com.benjamerc.spring_security_course.users.dto.request.UserPartialUpdateRequest;
import com.benjamerc.spring_security_course.users.dto.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.users.dto.response.UserProfileResponse;
import com.benjamerc.spring_security_course.security.core.CustomUserDetails;

public interface UserService {

    UserProfileResponse userProfile(CustomUserDetails userDetails);

    UserPartialUpdateResponse updateProfile(CustomUserDetails userDetails, UserPartialUpdateRequest request);

    void deleteAccount(CustomUserDetails userDetails);

    void logoutAll(CustomUserDetails userDetails);
}
