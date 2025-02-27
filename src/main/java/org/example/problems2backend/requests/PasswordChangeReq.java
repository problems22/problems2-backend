package org.example.problems2backend.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordChangeReq {
    private String username;
    private String oldPassword;
    private String newPassword;
}
