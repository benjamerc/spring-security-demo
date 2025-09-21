package com.benjamerc.spring_security_course.mapper;

import com.benjamerc.spring_security_course.domain.dto.user.response.UserPartialUpdateResponse;
import com.benjamerc.spring_security_course.domain.dto.user.response.UserProfileResponse;
import com.benjamerc.spring_security_course.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toUserProfileResponse(User user);

    UserPartialUpdateResponse toUserPartialUpdateResponse(User user);
}
