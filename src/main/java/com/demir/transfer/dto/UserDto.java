package com.demir.transfer.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private String username;
    private String password;
    private BigDecimal balance;
    private LocalDateTime created;
    private LocalDateTime updated;
}
