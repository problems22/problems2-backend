package org.example.problems2backend.responses;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRes {
    private String accessToken;
    private String refreshToken;
}
