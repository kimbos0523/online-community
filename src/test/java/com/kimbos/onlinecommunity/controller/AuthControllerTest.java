package com.kimbos.onlinecommunity.controller;

import com.kimbos.onlinecommunity.config.TestSecurityConfig;
import com.kimbos.onlinecommunity.service.ArticleService;
import com.kimbos.onlinecommunity.service.PaginationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View Controller - Authentication")
@Import(TestSecurityConfig.class)
@WebMvcTest(Void.class)
public class AuthControllerTest {

    private final MockMvc mvc;

    @MockBean private ArticleService articleService;
    @MockBean private PaginationService paginationService;

    public AuthControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    @DisplayName("[view][GET] Login Page - Ok Response")
    @Test
    public void viewGetLoginPageOkResponse() throws Exception {

        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        then(articleService).shouldHaveNoInteractions();
        then(paginationService).shouldHaveNoInteractions();
    }
}