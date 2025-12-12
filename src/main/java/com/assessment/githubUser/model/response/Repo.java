package com.assessment.githubUser.model.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
        "name",
        "url"})
public class Repo {
    String name;
    String url;
}
