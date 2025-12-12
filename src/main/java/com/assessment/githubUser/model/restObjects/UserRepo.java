package com.assessment.githubUser.model.restObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserRepo {
    @JsonProperty("name") String name;
    @JsonProperty("full_name") String full_name;
}
