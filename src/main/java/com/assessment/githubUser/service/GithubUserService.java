package com.assessment.githubUser.service;

import com.assessment.githubUser.model.response.Repo;
import com.assessment.githubUser.model.response.UserResponse;
import com.assessment.githubUser.model.restObjects.User;
import com.assessment.githubUser.model.restObjects.UserRepo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GithubUserService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${github.api.user-base-url}")
    private String githubUserApiUrlBase;

    @Value("${github.api.repo-base-url}")
    private String githubRepoUrlBase;

    @Cacheable(value = "userCache", key = "#username")
    public UserResponse getUserDetail(String username) throws ResponseStatusException {
        log.info("Making service call to Github user data for: {}", username);
        String userUrl = githubUserApiUrlBase + username;
        String userRepoUrl = githubUserApiUrlBase + username + "/repos";

        try {
            /** call GitHub User Detail API **/
            User gitUserDetail = restTemplate.getForEntity(userUrl, User.class).getBody();

            /** call GitHub User Repo Detail API **/
            List<UserRepo> gitUserRepoList = Arrays.asList(
                    Objects.requireNonNull(restTemplate.getForEntity(userRepoUrl, UserRepo[].class).getBody())
            );

            /** Assumption: Git user detail is mandatory; Git user repo detail is optional - throwing a warning in this case **/
            if (gitUserDetail == null) {
                throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No user data found for: " + username);
            }
            if (gitUserRepoList.isEmpty()) {
                log.warn("No user repo data found for: " + username);
            }

            ArrayList<Repo> repos = gitUserRepoList.stream()
                    .filter(Objects::nonNull)
                    .map(r -> {
                        Repo repo = new Repo();
                        repo.setName(r.getName());
                        repo.setUrl(r.getFull_name() == null ? null : (githubRepoUrlBase + r.getFull_name()));
                        return repo;
                    })
                    .collect(Collectors.toCollection(ArrayList::new));


            String formattedDate = null;
            if (gitUserDetail.getCreated_at() != null) {
                formattedDate = dateFormatter(gitUserDetail.getCreated_at());
            }

            /** Assemble and return service response **/
            return UserResponse.builder().user_name(gitUserDetail.getLogin())
                    .display_name(gitUserDetail.getName())
                    .avatar(gitUserDetail.getAvatar_url())
                    .geo_location(gitUserDetail.getLocation())
                    .email(gitUserDetail.getEmail())
                    .url(gitUserDetail.getUrl())
                    .created_at(formattedDate).repos(repos).build();


        } catch (HttpClientErrorException e){
            log.info("Error getting user data for: {}", username, e);
            if (e.getStatusCode() == HttpStatus.NOT_FOUND){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No User Found", e);
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting user data", e);
            }
        }
    }

    private String dateFormatter(String date){

        Instant instant = Instant.parse(date);
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

        return formatter.withZone(ZoneOffset.UTC).format(instant);
    }
}
