package com.kimbos.onlinecommunity.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@Disabled("Spring Data Rest Test is unnecessary excluded")
@DisplayName("Data Rest API Test")
@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class DataRestTest {

    private final MockMvc mvc;

    public DataRestTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    @DisplayName("Retrieve Article List")
    @Test
    void getArticlesTest() throws Exception {

        mvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }

    @DisplayName("Retrieve an Article")
    @Test
    void getArticleTest() throws Exception {

        mvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }

    @DisplayName("Retrieve Comment List from Article")
    @Test
    void getCommentsFromArticleTest() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/api/articles/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }

    @DisplayName("Retrieve Comment List")
    @Test
    void getCommentsTest() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }

    @DisplayName("Retrieve a Comment")
    @Test
    void getComment() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/api/comments/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }
}
