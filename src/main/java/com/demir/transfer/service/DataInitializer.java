package com.demir.transfer.service;

import com.demir.transfer.entity.Role;
import com.demir.transfer.entity.User;
import com.demir.transfer.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Set;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserService userService;

    @Autowired
    public DataInitializer(RoleRepository roleRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @PostConstruct
    public void Init() {

        roleRepository.saveAll(Set.of(new Role("ADMIN")));
        roleRepository.saveAll(Set.of(new Role("USER")));

        User user = new User();
        user.setUsername("Mirseit");
        user.setPassword("1111");
        userService.addUser(user);

        User user1 = new User();
        user1.setUsername("Mir");
        user1.setPassword("1234");
        userService.addUser(user1);
    }
}

