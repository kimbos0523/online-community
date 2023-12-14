package com.kimbos.onlinecommunity.controller;

import com.kimbos.onlinecommunity.dto.request.CommentRequest;
import com.kimbos.onlinecommunity.dto.UserAccountDto;
import com.kimbos.onlinecommunity.dto.security.CommunityPrincipal;
import com.kimbos.onlinecommunity.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class CommentController {

    private final CommentService commentService;


    @PostMapping("/new")
    public String postNewComment(
            @AuthenticationPrincipal CommunityPrincipal communityPrincipal,
            CommentRequest commentRequest
    ) {
        commentService.saveComment(commentRequest.toDto(communityPrincipal.toDto()));

        return "redirect:/articles/" + commentRequest.articleId();
    }

    @PostMapping ("/{commentId}/delete")
    public String deleteArticleComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CommunityPrincipal communityPrincipal,
            Long articleId
    ) {
        commentService.deleteComment(commentId, communityPrincipal.getUsername());

        return "redirect:/articles/" + articleId;
    }
}

