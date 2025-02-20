package org.example.problems2backend.requests;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthReq {
    private String username;
    private String password;
}
