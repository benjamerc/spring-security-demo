package com.benjamerc.spring_security_course.mapper;

import com.benjamerc.spring_security_course.domain.dto.auth.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.domain.dto.auth.response.AuthRegisterResponse;
import com.benjamerc.spring_security_course.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthenticationMapper {

    AuthRegisterResponse toAuthRegisterResponse(User user);

}
