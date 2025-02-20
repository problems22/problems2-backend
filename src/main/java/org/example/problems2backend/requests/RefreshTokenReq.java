package org.example.problems2backend.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenReq {
    private String refreshToken;
}
