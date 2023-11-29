package com.kimbos.onlinecommunity.dto;

import com.kimbos.onlinecommunity.domain.Article;
import com.kimbos.onlinecommunity.domain.Comment;
import com.kimbos.onlinecommunity.domain.UserAccount;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        Long articleId,
        UserAccountDto userAccountDto,
        String content,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {

    public static CommentDto of(Long articleId, UserAccountDto userAccountDto, String content) {
        return new CommentDto(null, articleId, userAccountDto, content, null, null, null, null);
    }

    public static CommentDto of(Long id, Long articleId, UserAccountDto userAccountDto, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new CommentDto(id, articleId, userAccountDto, content, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static CommentDto from(Comment entity) {
        return new CommentDto(
                entity.getId(),
                entity.getArticle().getId(),
                UserAccountDto.from(entity.getUserAccount()),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getModifiedAt(),
                entity.getModifiedBy()
        );
    }

    public Comment toEntity(Article entity, UserAccount userAccount) {
        return Comment.of(
                entity,
                userAccount,
                content
        );
    }
}