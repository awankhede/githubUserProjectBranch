package com.assessment.githubUser.service;

import com.assessment.githubUser.model.response.UserResponse;
import com.assessment.githubUser.model.restObjects.User;
import com.assessment.githubUser.model.restObjects.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubUserServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GithubUserService githubUserService;

    @Test
    void getUserDetail_success_buildsResponseAndMapsRepos() {
        String username = "octocat";
        String userUrl = "https://api.github.com/users/" + username;
        String userRepoUrl = "https://api.github.com/users/" + username + "/repos";

        User user = new User();
        user.setLogin("octocat");
        user.setName("The Octocat");
        user.setAvatar_url("https://avatars.example/octo.png");
        user.setLocation("Internet");
        user.setEmail("octo@example.com");
        user.setUrl("https://api.github.com/users/octocat");
        user.setCreated_at("2020-01-01T00:00:00Z");

        UserRepo repo1 = new UserRepo();
        repo1.setName("repo-one");
        repo1.setFull_name("octocat/repo-one");

        UserRepo repo2 = new UserRepo();
        repo2.setName("repo-two");
        repo2.setFull_name("octocat/repo-two");

        when(restTemplate.getForEntity(eq(userUrl), eq(User.class)))
                .thenReturn(ResponseEntity.ok(user));

        when(restTemplate.getForEntity(eq(userRepoUrl), eq(UserRepo[].class)))
                .thenReturn(ResponseEntity.ok(new UserRepo[]{repo1, repo2}));

        UserResponse response = githubUserService.getUserDetail(username);

        assertNotNull(response);
        assertEquals("octocat", response.getUser_name());
        assertEquals("The Octocat", response.getDisplay_name());
        assertEquals("https://avatars.example/octo.png", response.getAvatar());
        assertEquals("Internet", response.getGeo_location());
        assertEquals("octo@example.com", response.getEmail());
        assertEquals("https://api.github.com/users/octocat", response.getUrl());
        assertNotNull(response.getCreated_at());

        assertNotNull(response.getRepos());
        assertEquals(2, response.getRepos().size());
        assertEquals("repo-one", response.getRepos().get(0).getName());
        assertEquals("https://api.github.com/repos/octocat/repo-one", response.getRepos().getFirst().getUrl());
    }

    @Test
    void getUserDetail_whenGitHubReturns404_throwsNotFound() {
        String username = "missing-user";
        String userUrl = "https://api.github.com/users/" + username;

        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "nope".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(restTemplate.getForEntity(eq(userUrl), eq(User.class)))
                .thenThrow(notFound);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> githubUserService.getUserDetail(username));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getUserDetail_whenGitHubReturnsOtherError_throwsInternalServerError() {
        String username = "rate-limited";
        String userUrl = "https://api.github.com/users/" + username;

        HttpClientErrorException tooManyRequests = HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                null,
                "rate limited".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(restTemplate.getForEntity(eq(userUrl), eq(User.class)))
                .thenThrow(tooManyRequests);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> githubUserService.getUserDetail(username));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }
}