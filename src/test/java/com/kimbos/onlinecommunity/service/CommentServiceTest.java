package com.kimbos.onlinecommunity.service;

import com.kimbos.onlinecommunity.domain.Article;
import com.kimbos.onlinecommunity.domain.Comment;
import com.kimbos.onlinecommunity.domain.UserAccount;
import com.kimbos.onlinecommunity.dto.CommentDto;
import com.kimbos.onlinecommunity.dto.UserAccountDto;
import com.kimbos.onlinecommunity.repository.ArticleRepository;
import com.kimbos.onlinecommunity.repository.CommentRepository;
import com.kimbos.onlinecommunity.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("Business Logic - Comment")
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks private CommentService commentService;

    @Mock private CommentRepository commentRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private UserAccountRepository userAccountRepository;


    @DisplayName("Search Article by ID -> return comment list")
    @Test
    void searchCommentByArticleIdTest() {

        Long articleId = 1L;
        Comment expectedComment = createComment("content");
        given(commentRepository.findByArticle_Id(articleId)).willReturn(List.of(expectedComment));

        List<CommentDto> actual = commentService.searchComments(articleId);

        assertThat(actual)
                .hasSize(1)
                .first().hasFieldOrPropertyWithValue("content", expectedComment.getContent());
        then(commentRepository).should().findByArticle_Id(articleId);
    }

    @DisplayName("Input comment information -> create a comment")
    @Test
    void inputCommentInformationCreateComment() {

        CommentDto commentDto = createCommentDto("comment");
        given(articleRepository.getReferenceById(commentDto.articleId())).willReturn(createArticle());
        given(commentRepository.save(any(Comment.class))).willReturn(null);

        commentService.saveComment(commentDto);

        then(articleRepository).should().getReferenceById(commentDto.articleId());
        then(commentRepository).should().save(any(Comment.class));
    }

    @DisplayName("Try Save New Comment -> Do Nothing Give Log If Nonexistent Article")
    @Test
    void trySaveNewCommentDoNothingGiveLogIfNonexistentArticle() {

        CommentDto dto = createCommentDto("comment");
        given(articleRepository.getReferenceById(dto.articleId())).willThrow(EntityNotFoundException.class);

        commentService.saveComment(dto);

        then(articleRepository).should().getReferenceById(dto.articleId());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoInteractions();
    }

    @DisplayName("Input New Comment Information -> Update Comment")
    @Test
    void inputNewCommentInfoUpdateComment() {

        String prevContent = "content";
        String newContent = "comment";
        Comment comment = createComment(prevContent);
        CommentDto dto = createCommentDto(newContent);
        given(commentRepository.getReferenceById(dto.id())).willReturn(comment);

        commentService.updateComment(dto);

        assertThat(comment.getContent())
                .isNotEqualTo(prevContent)
                .isEqualTo(newContent);
        then(commentRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("없는 댓글 정보를 수정하려고 하면, 경고 로그를 찍고 아무 것도 안 한다.")
    @Test
    void givenNonexistentComment_whenUpdatingComment_thenLogsWarningAndDoesNothing() {

        CommentDto dto = createCommentDto("댓글");
        given(commentRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

        commentService.updateComment(dto);

        then(commentRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("Input CommentId -> Delete Comment.")
    @Test
    void inputCommentIdDeleteComment() {

        Long commentId = 1L;
        String userId = "kim";
        willDoNothing().given(commentRepository).deleteByIdAndUserAccount_UserId(commentId, userId);

        commentService.deleteComment(commentId, userId);

        then(commentRepository).should().deleteByIdAndUserAccount_UserId(commentId, userId);
    }

    /**********************************************/
    /********** Private Methods for Test **********/
    /**********************************************/

    private CommentDto createCommentDto(String content) {
        return CommentDto.of(1L,
                1L,
                createUserAccountDto(),
                content,
                LocalDateTime.now(),
                "kimbos",
                LocalDateTime.now(),
                "kimbos");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "uno",
                "password",
                "uno@mail.com",
                "Uno",
                "This is memo",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno"
        );
    }

    private Comment createComment(String content) {
        return Comment.of(
                Article.of(createUserAccount(), "title", "content", "hashtag"),
                createUserAccount(),
                content
        );
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "uno",
                "password",
                "uno@email.com",
                "Uno",
                null
        );
    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content",
                "#java"
        );
    }
}