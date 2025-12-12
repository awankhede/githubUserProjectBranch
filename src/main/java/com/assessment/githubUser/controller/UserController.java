package com.assessment.githubUser.controller;

import com.assessment.githubUser.model.response.UserResponse;
import com.assessment.githubUser.service.GithubUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

    @Autowired
    private GithubUserService githubUserService;

    @GetMapping("/users/{username}")
    public ResponseEntity<UserResponse> getUerDetail(@PathVariable String username){
        try {
            if (username == null || username.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be empty");
            }
            UserResponse userResponse = githubUserService.getUserDetail(username);
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        }  catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Runtime error", e);
        }
    }
}
