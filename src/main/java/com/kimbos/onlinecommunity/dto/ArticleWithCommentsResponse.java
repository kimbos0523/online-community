package com.kimbos.onlinecommunity.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.LinkedHashSet;

public record ArticleWithCommentsResponse(
        Long id,
        String title,
        String content,
        String hashtag,
        LocalDateTime createdAt,
        String email,
        String nickname,
        String userId,
        Set<CommentResponse> commentsResponse
) {

    public static ArticleWithCommentsResponse of(Long id,
                                                 String title,
                                                 String content,
                                                 String hashtag,
                                                 LocalDateTime createdAt,
                                                 String email,
                                                 String nickname,
                                                 String userId,
                                                 Set<CommentResponse> commentResponses) {
        return new ArticleWithCommentsResponse(id,
                title,
                content,
                hashtag,
                createdAt,
                email,
                nickname,
                userId,
                commentResponses);
    }

    public static ArticleWithCommentsResponse from(ArticleWithCommentsDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.userAccountDto().userId();
        }

        return new ArticleWithCommentsResponse(
                dto.id(),
                dto.title(),
                dto.content(),
                dto.hashtag(),
                dto.createdAt(),
                dto.userAccountDto().email(),
                nickname,
                dto.userAccountDto().userId(),
                dto.articleCommentDtos().stream()
                        .map(CommentResponse::from)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }
}
