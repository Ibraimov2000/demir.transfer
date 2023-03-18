package com.demir.transfer.service;

import com.demir.transfer.entity.User;
import com.demir.transfer.exception.InsufficientBalanceException;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.naming.AuthenticationException;
import javax.security.auth.login.AccountLockedException;

public interface UserService extends UserDetailsService {
    String login(String username, String password) throws AuthenticationException, AccountLockedException;
    void logout(String token);
    User payment(String token) throws AuthenticationException, InsufficientBalanceException;
    User addUser(User user);
}
