package com.example.userservice;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-service")
public class UsersController {

    private final ModelMapper modelMapper;
    private final UserService userService;
    private final Environment env;

    @Value("${greeting.message}")
    private String message;

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);

        userService.createUser(userDto);

        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/health-check")
    public String status() { return String.format("It's working in User Service on PORT %s", env.getProperty("local.server.port")); }

    @GetMapping("/welcome")
    public String welcome() {
        return message;
    }
}
