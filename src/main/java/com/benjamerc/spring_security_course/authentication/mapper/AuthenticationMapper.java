package com.benjamerc.spring_security_course.authentication.mapper;

import com.benjamerc.spring_security_course.authentication.dto.response.AuthRegisterResponse;
import com.benjamerc.spring_security_course.users.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthenticationMapper {

    AuthRegisterResponse toAuthRegisterResponse(User user);

}
