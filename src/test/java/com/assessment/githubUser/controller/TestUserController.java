package com.assessment.githubUser.controller;

import com.assessment.githubUser.model.response.UserResponse;
import com.assessment.githubUser.service.GithubUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GithubUserService githubUserService;

    @Test
    void getUserDetail_whenServiceReturnsOk_returns200AndBody() throws Exception {
        String username = "octocat";

        UserResponse response = UserResponse.builder()
                .user_name("octocat")
                .display_name("The Octocat")
                .avatar("https://avatars.example/octo.png")
                .geo_location("Internet")
                .email("octo@example.com")
                .url("https://api.github.com/users/octocat")
                .created_at("Wed, 01 Jan 2020 00:00:00 GMT")
                .repos(null)
                .build();

        when(githubUserService.getUserDetail(eq(username))).thenReturn(response);

        mockMvc.perform(get("/users/{username}", username)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user_name").value("octocat"))
                .andExpect(jsonPath("$.display_name").value("The Octocat"));
    }

    @Test
    void getUserDetail_whenServiceThrowsRuntimeException_returns500() throws Exception {
        String username = "octocat";

        when(githubUserService.getUserDetail(eq(username)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/users/{username}", username)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
