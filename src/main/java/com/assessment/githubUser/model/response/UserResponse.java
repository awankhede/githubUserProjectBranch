package com.assessment.githubUser.model.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
@JsonPropertyOrder({
        "user_name",
        "display_name",
        "avatar",
        "geo_location",
        "email",
        "url",
        "created_at",
        "repos"
})
public class UserResponse {
    String user_name;
    String display_name;
    String avatar;
    String geo_location;
    String email;
    String url;
    String created_at;
    ArrayList<Repo> repos;
}

