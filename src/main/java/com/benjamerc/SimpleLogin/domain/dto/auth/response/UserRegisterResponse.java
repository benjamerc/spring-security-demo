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
public class UserRegisterResponse {

    private Long id;

    private String username;

    private String email;

    private Role role;
}
