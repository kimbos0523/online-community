package com.kimbos.onlinecommunity.controller;

import com.kimbos.onlinecommunity.config.TestSecurityConfig;
import com.kimbos.onlinecommunity.dto.CommentDto;
import com.kimbos.onlinecommunity.dto.request.CommentRequest;
import com.kimbos.onlinecommunity.service.CommentService;
import com.kimbos.onlinecommunity.utils.FormDataEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View Controller - Comment")
@Import({TestSecurityConfig.class, FormDataEncoder.class})
@WebMvcTest(CommentController.class)
class CommentControllerTest {

    private final MockMvc mvc;

    private final FormDataEncoder formDataEncoder;
    @MockBean private CommentService commentService;

    public CommentControllerTest(
            @Autowired MockMvc mvc,
            @Autowired FormDataEncoder formDataEncoder
    ) {
        this.mvc = mvc;
        this.formDataEncoder = formDataEncoder;
    }


    @WithUserDetails(value = "kim", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] Save Comment - Ok Response")
    @Test
    void viewPostSaveCommentOkResponse() throws Exception {

        long articleId = 1L;
        CommentRequest commentRequest = CommentRequest.of(articleId, "test comment");
        willDoNothing().given(commentService).saveComment(any(CommentDto.class));

        mvc.perform(post("/comments/new")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(formDataEncoder.encode(commentRequest))
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/" + articleId))
                .andExpect(redirectedUrl("/articles/" + articleId));

        then(commentService).should().saveComment(any(CommentDto.class));
    }

    @WithUserDetails(value = "kim", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][GET] Delete Comment - Ok Response")
    @Test
    void viewGetDeleteCommentOkResponse() throws Exception {

        long articleId = 1L;
        long commentId = 1L;
        String userId = "kimbos";
        willDoNothing().given(commentService).deleteComment(commentId, userId);

        mvc.perform(
                        post("/comments/" + commentId + "/delete")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(Map.of("articleId", articleId)))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/" + articleId))
                .andExpect(redirectedUrl("/articles/" + articleId));

        then(commentService).should().deleteComment(commentId, userId);
    }

    @WithUserDetails(value = "kim", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("view][GET] Save Reply - Ok Response")
    @Test
    void viewGetSaveReplyOkResponse() throws Exception {

        long articleId = 1L;
        CommentRequest request = CommentRequest.of(articleId, 1L, "test comment");
        willDoNothing().given(commentService).saveComment(any(CommentDto.class));

        mvc.perform(
                        post("/comments/new")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(request))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/" + articleId))
                .andExpect(redirectedUrl("/articles/" + articleId));
        then(commentService).should().saveComment(any(CommentDto.class));
    }
}