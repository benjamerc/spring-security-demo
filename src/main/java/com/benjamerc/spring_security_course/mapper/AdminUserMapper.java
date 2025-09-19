package com.benjamerc.spring_security_course.mapper;

import com.benjamerc.spring_security_course.domain.dto.admin.user.response.AdminUserResponse;
import com.benjamerc.spring_security_course.domain.dto.admin.user.response.AdminUserSummaryResponse;
import com.benjamerc.spring_security_course.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    AdminUserResponse toAdminUserResponse(User user);

    AdminUserSummaryResponse toAdminUserSummaryResponse(User user);
}
