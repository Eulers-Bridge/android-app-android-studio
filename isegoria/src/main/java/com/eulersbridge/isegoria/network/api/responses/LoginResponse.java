package com.eulersbridge.isegoria.network.api.responses;

import com.eulersbridge.isegoria.network.api.models.NewsArticle;
import com.eulersbridge.isegoria.network.api.models.User;

import java.util.List;

public class LoginResponse {

    public List<NewsArticle> articles;
    public User user;
    public long userId;

}
