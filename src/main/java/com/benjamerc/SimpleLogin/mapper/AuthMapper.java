package com.benjamerc.SimpleLogin.mapper;

import com.benjamerc.SimpleLogin.domain.dto.auth.request.UserLoginRequest;
import com.benjamerc.SimpleLogin.domain.dto.auth.request.UserRegisterRequest;
import com.benjamerc.SimpleLogin.domain.dto.auth.response.UserLoginResponse;
import com.benjamerc.SimpleLogin.domain.dto.auth.response.UserRegisterResponse;
import com.benjamerc.SimpleLogin.domain.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserEntity registerRequestToEntity(UserRegisterRequest request);

    UserRegisterResponse entityToRegisterResponse(UserEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserEntity loginRequestToEntity(UserLoginRequest request);

    UserLoginResponse entityToLoginResponse(UserEntity entity);
}
