package com.demir.transfer.rest;

import com.demir.transfer.dto.TokenDto;
import com.demir.transfer.dto.UserDto;
import com.demir.transfer.entity.User;
import com.demir.transfer.exception.InsufficientBalanceException;
import com.demir.transfer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

import java.security.Principal;

import static com.demir.transfer.service.UserServiceImpl.getKeyByValue;
import static com.demir.transfer.service.UserServiceImpl.getLoggedUsers;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestParam("username") String username, @RequestParam("password") String password) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = userService.login(username, password);
        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken(token);
        return ResponseEntity.ok(tokenDto);
    }

    @GetMapping("/logout")
    public ResponseEntity logout(Principal principal) {
        String token = getKeyByValue(getLoggedUsers(),principal.getName());
        userService.logout(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payment")
    public ResponseEntity<UserDto> payment(Principal principal) throws AuthenticationException, InsufficientBalanceException {
        String token = getKeyByValue(getLoggedUsers(), principal.getName());
        User user = userService.payment(token);

        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setBalance(user.getBalance());
        userDto.setCreated(user.getCreated());
        userDto.setUpdated(user.getUpdated());

        return ResponseEntity.ok().body(userDto);
    }
}
