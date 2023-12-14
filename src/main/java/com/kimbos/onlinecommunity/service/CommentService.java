package com.kimbos.onlinecommunity.service;

import com.kimbos.onlinecommunity.domain.Article;
import com.kimbos.onlinecommunity.domain.Comment;
import com.kimbos.onlinecommunity.domain.UserAccount;
import com.kimbos.onlinecommunity.dto.CommentDto;
import com.kimbos.onlinecommunity.repository.ArticleRepository;
import com.kimbos.onlinecommunity.repository.CommentRepository;
import com.kimbos.onlinecommunity.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CommentService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserAccountRepository userAccountRepository;


    @Transactional(readOnly = true)
    public List<CommentDto> searchComments(Long articleId) {
        return commentRepository.findByArticle_Id(articleId)
                .stream()
                .map(CommentDto::from)
                .toList();
    }

    public void saveComment(CommentDto commentDto) {
        try {
            Article article = articleRepository.getReferenceById(commentDto.articleId());
            UserAccount userAccount = userAccountRepository.getReferenceById(commentDto.userAccountDto().userId());
            Comment comment = commentDto.toEntity(article, userAccount);

            if (commentDto.parentCommentId() != null) {
                Comment parentComment = commentRepository.getReferenceById(commentDto.parentCommentId());
                parentComment.addChildComment(comment);
            } else {
                commentRepository.save(comment);
            }
        } catch (EntityNotFoundException enf) {
            log.warn("Fail to save the comment - Cannot find the article or user account - dto: {}", commentDto);
        }
    }

    public void updateComment(CommentDto commentDto) {
        try {
            Comment comment = commentRepository.getReferenceById(commentDto.id());
            if (commentDto.content() != null) { comment.setContent(commentDto.content()); }
        } catch (EntityNotFoundException e) {
            log.warn("Fail to update the comment - Cannot find the article - dto: {}", commentDto);
        }
    }

    public void deleteComment(Long commentId, String userId) {
        commentRepository.deleteByIdAndUserAccount_UserId(commentId, userId);
    }
}
