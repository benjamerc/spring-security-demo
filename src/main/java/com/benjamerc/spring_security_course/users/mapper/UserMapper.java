package com.benjamerc.spring_security_course.users.mapper;

import com.benjamerc.spring_security_course.users.dto.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.users.dto.response.UserProfileResponse;
import com.benjamerc.spring_security_course.users.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toUserProfileResponse(User user);

    UserPartialUpdateResponse toUserPartialUpdateResponse(User user);
}
