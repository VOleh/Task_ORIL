package com.vanivskyi.test.controller;

import com.vanivskyi.test.dto.UserCreateDTO;
import com.vanivskyi.test.dto.UserReadDTO;
import com.vanivskyi.test.dto.UserUpdateDTO;
import com.vanivskyi.test.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test/app")
public class UserRESTApiController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public HttpStatus createUser(@RequestBody UserCreateDTO userDTO) {
        userService.validateRegistrationRequest(userDTO);
        userService.saveUser(userDTO);

        return HttpStatus.OK;
    }

    @GetMapping("/get/{email}")
    public UserReadDTO getUser(@PathVariable("email") String email) {
        return userService.getUser(email);
    }

    @GetMapping("/getAll")
    public List<UserReadDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/delete/{email}")
    public String deleteUser(@PathVariable("email") String email) {
        return userService.deleteUser(email);
    }

    @PutMapping("/update")
    public UserReadDTO updateUser(@RequestBody UserUpdateDTO userDTO) {
        return userService.updateUser(userDTO);
    }
}