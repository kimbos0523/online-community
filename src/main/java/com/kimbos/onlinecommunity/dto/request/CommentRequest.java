package com.kimbos.onlinecommunity.dto.request;

import com.kimbos.onlinecommunity.dto.CommentDto;
import com.kimbos.onlinecommunity.dto.UserAccountDto;

public record CommentRequest(
        Long articleId,
        Long parentCommentId,
        String content
) {

    public static CommentRequest of(Long articleId, String content) {
        return new CommentRequest(articleId, null, content);
    }

    public static CommentRequest of(Long articleId, Long parentCommentId, String content) {
        return new CommentRequest(articleId, parentCommentId, content);
    }

    public CommentDto toDto(UserAccountDto userAccountDto) {
        return CommentDto.of(
                articleId,
                userAccountDto,
                parentCommentId,
                content
        );
    }

}