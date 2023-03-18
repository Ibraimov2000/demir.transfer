package com.demir.transfer.service;

import com.demir.transfer.entity.Payment;
import com.demir.transfer.entity.Role;
import com.demir.transfer.entity.User;
import com.demir.transfer.exception.InsufficientBalanceException;
import com.demir.transfer.repository.PaymentReposiotory;
import com.demir.transfer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import javax.security.auth.login.AccountLockedException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final PaymentReposiotory paymentReposiotory;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Map<String, String> loggedUsers = new HashMap<>();

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;


    @Autowired
    public UserServiceImpl(PaymentReposiotory paymentReposiotory, UserRepository userRepository) {
        this.paymentReposiotory = paymentReposiotory;
        this.userRepository = userRepository;
    }

    @Override
    public User addUser(User user) {
        user.setBalance(BigDecimal.valueOf(8));
        user.setRoles(Set.of(new Role("USER")));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        User addedUser = userRepository.save(user);

        log.info("IN addUser - user: {} successfully added", addedUser);
        return addedUser;
    }

    @Override
    public String login(String username, String password) throws AuthenticationException, AccountLockedException {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            throw new AuthenticationException("Invalid username or password");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            handleFailedLogin(user);
            throw new AuthenticationException("Invalid username or password");
        }

        resetFailedLoginAttempts(user);

        String token = UUID.randomUUID().toString();
        loggedUsers.put(token, username);
        log.info("IN login token created: {} successfully", token);
        return token;
    }

    @Override
    public void logout(String token) {
        loggedUsers.remove(token);
        log.info("IN logout token: {} was removed successfully", token);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public User payment(String token) throws AuthenticationException, InsufficientBalanceException {
        String username = loggedUsers.get(token);

        if (username == null) {
            throw new AuthenticationException("User is not logged in");
        }

        User user = userRepository.findUserByUsername(username);

        if (user.getBalance().compareTo(BigDecimal.valueOf(1.1)) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(BigDecimal.valueOf(1.1));
        payment.setCreated(LocalDateTime.now());
        paymentReposiotory.save(payment);

        user.setBalance((user.getBalance().subtract(payment.getAmount())));
        user.setUpdated(LocalDateTime.now());

        log.info("IN payment: {} was created and user: {} was updated", payment, user);
        return userRepository.save(user);
    }

    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Map<String, String> getLoggedUsers() {
        return loggedUsers;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("user: " + username + " found");
        }
        return user;
    }

    private void handleFailedLogin(User user) throws AccountLockedException {
        int failedLoginAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedLoginAttempts);
        if (failedLoginAttempts >= MAX_LOGIN_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(ACCOUNT_LOCK_DURATION_MINUTES));
            userRepository.save(user);
            throw new AccountLockedException("Too many failed login attempts. Account locked for " + ACCOUNT_LOCK_DURATION_MINUTES + " minutes.");
        }
        userRepository.save(user);
    }

    private void resetFailedLoginAttempts(User user) throws AccountLockedException {
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException("Account is locked until " + user.getLockedUntil() + ".");
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
}
