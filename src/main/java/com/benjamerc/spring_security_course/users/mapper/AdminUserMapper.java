package com.benjamerc.spring_security_course.users.mapper;

import com.benjamerc.spring_security_course.users.dto.response.AdminUserResponse;
import com.benjamerc.spring_security_course.users.dto.response.AdminUserSummaryResponse;
import com.benjamerc.spring_security_course.users.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    AdminUserResponse toAdminUserResponse(User user);

    AdminUserSummaryResponse toAdminUserSummaryResponse(User user);
}
