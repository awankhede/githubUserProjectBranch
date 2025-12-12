package com.assessment.githubUser.model.restObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class User {
    @JsonProperty("login") String login;
    @JsonProperty("name") String name;
    @JsonProperty("avatar_url") String avatar_url;
    @JsonProperty("location") String location;
    @JsonProperty("email") String email;
    @JsonProperty("url") String url;
    @JsonProperty("created_at") String created_at;
}
