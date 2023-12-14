package com.kimbos.onlinecommunity.service;

import com.kimbos.onlinecommunity.domain.Article;
import com.kimbos.onlinecommunity.domain.Comment;
import com.kimbos.onlinecommunity.domain.Hashtag;
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
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
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
        Comment expectedParentComment = createComment(1L, "parent");
        Comment expectedChildComment = createComment(2L, "child");
        expectedChildComment.setParentCommentId(expectedParentComment.getId());
        given(commentRepository.findByArticle_Id(articleId)).willReturn(List.of(
                expectedParentComment,
                expectedChildComment
        ));

        List<CommentDto> actual = commentService.searchComments(articleId);

        assertThat(actual).hasSize(2);
        assertThat(actual)
                .extracting("id", "articleId", "parentCommentId", "content")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, null, "parent"),
                        tuple(2L, 1L, 1L, "child")
                );
        then(commentRepository).should().findByArticle_Id(articleId);
    }

    @DisplayName("Input comment information -> create a comment")
    @Test
    void inputCommentInformationCreateComment() {

        CommentDto commentDto = createCommentDto("comment");
        given(articleRepository.getReferenceById(commentDto.articleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(commentDto.userAccountDto().userId())).willReturn(createUserAccount());
        given(commentRepository.save(any(Comment.class))).willReturn(null);

        commentService.saveComment(commentDto);

        then(articleRepository).should().getReferenceById(commentDto.articleId());
        then(userAccountRepository).should().getReferenceById(commentDto.userAccountDto().userId());
        then(commentRepository).should(never()).getReferenceById(anyLong());
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

    @DisplayName("Input Parent Comment Id and New Comment Information -> Update Comment")
    @Test
    void inputParentCommentIdNewCommentInfoUpdateComment() {

        Long parentCommentId = 1L;
        Comment parent = createComment(parentCommentId, "comment");
        CommentDto child  = createCommentDto(parentCommentId, "reply");
        given(articleRepository.getReferenceById(child.articleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(child.userAccountDto().userId())).willReturn(createUserAccount());
        given(commentRepository.getReferenceById(child.parentCommentId())).willReturn(parent);

        commentService.saveComment(child);

        assertThat(child.parentCommentId()).isNotNull();
        then(articleRepository).should().getReferenceById(child.articleId());
        then(userAccountRepository).should().getReferenceById(child.userAccountDto().userId());
        then(commentRepository).should().getReferenceById(child.parentCommentId());
        then(commentRepository).should(never()).save(any(Comment.class));
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
        return createCommentDto(null, content);
    }

    private CommentDto createCommentDto(Long parentCommentId, String content) {
        return createCommentDto(1L, parentCommentId, content);
    }

    private CommentDto createCommentDto(Long id, Long parentCommentId, String content) {
        return CommentDto.of(
                id,
                1L,
                createUserAccountDto(),
                parentCommentId,
                content,
                LocalDateTime.now(),
                "kim",
                LocalDateTime.now(),
                "kim"
        );
    }


    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "kim",
                "password",
                "kimbos0523@gmail.com",
                "kim",
                "test memo",
                LocalDateTime.now(),
                "kim",
                LocalDateTime.now(),
                "kim"
        );
    }

    private Comment createComment(Long id, String content) {
        Comment comment = Comment.of(
                createArticle(),
                createUserAccount(),
                content
        );
        ReflectionTestUtils.setField(comment, "id", id);

        return comment;
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "kim",
                "password",
                "kim@email.com",
                "Kim",
                null
        );
    }

    private Article createArticle() {
        Article article = Article.of(
                createUserAccount(),
                "title",
                "content"
        );
        ReflectionTestUtils.setField(article, "id", 1L);
        article.addHashtags(Set.of(createHashtag(article)));

        return article;
    }

    private Hashtag createHashtag(Article article) {
        return Hashtag.of("java");
    }
}