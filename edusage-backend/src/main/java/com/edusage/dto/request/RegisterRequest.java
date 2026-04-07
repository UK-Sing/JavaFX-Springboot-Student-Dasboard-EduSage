package com.edusage.dto.request;

import com.edusage.model.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private Role role;
    private String rollNo;
    private String department;
}
