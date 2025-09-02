package com.benjamerc.SimpleLogin.domain.dto.auth.response;

import com.benjamerc.SimpleLogin.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginResponse {

    private String username;

    private Role role;
}
