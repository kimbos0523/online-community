package com.kimbos.onlinecommunity.service;

import com.kimbos.onlinecommunity.domain.Article;
import com.kimbos.onlinecommunity.domain.UserAccount;
import com.kimbos.onlinecommunity.domain.enums.SearchType;
import com.kimbos.onlinecommunity.dto.ArticleDto;
import com.kimbos.onlinecommunity.dto.ArticleWithCommentsDto;
import com.kimbos.onlinecommunity.dto.UserAccountDto;
import com.kimbos.onlinecommunity.repository.ArticleRepository;
import com.kimbos.onlinecommunity.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("Business Logic - Article")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks private ArticleService articleService;

    @Mock private ArticleRepository articleRepository;
    @Mock private UserAccountRepository userAccountRepository;

    @DisplayName("Search Article Without Parameter -> Return Articles Page")
    @Test
    void searchArticleWithoutParamsReturnArticlesPage() {

        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());

        Page<ArticleDto> articles = articleService.searchArticles(null, null, pageable);

        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("Search Article With Parameter -> Return Article Page")
    @Test
    void searchArticleWithParamsReturnArticlePage() {

        SearchType searchType = SearchType.TITLE;
        String searchKeyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitleContaining(searchKeyword, pageable)).willReturn(Page.empty());

        Page<ArticleDto> articles = articleService.searchArticles(searchType, searchKeyword, pageable);

        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitleContaining(searchKeyword, pageable);
    }

    @DisplayName("Look Up Article by Article Id -> Return Article")
    @Test
    void lookUpArticleIdReturnArticle() {

        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        ArticleWithCommentsDto dto = articleService.getArticleWithComments(articleId);

        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("Look Up Nonexistent Article by Article Id -> Throw Exception")
    @Test
    void lookUpNonexistentArticleIdThrowException() {

        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        Throwable t = catchThrowable(() -> articleService.getArticle(articleId));

        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("There is no article - articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("Search Article By Hashtag Without Parameter -> Return Empty Page")
    @Test
    void searchArticleByHashTagWithoutParamReturnEmptyPage() {

        Pageable pageable = Pageable.ofSize(20);

        Page<ArticleDto> articles = articleService.searchArticlesByHashtag(null, pageable);

        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).shouldHaveNoInteractions();
    }

    @DisplayName("Search Article By Hashtag With Parameter -> Return Articles Page")
    @Test
    void searchArticleByHashTagWithParamReturnArticlesPage() {

        String hashtag = "#java spring";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtag(hashtag, pageable)).willReturn(Page.empty(pageable));

        Page<ArticleDto> articles = articleService.searchArticlesByHashtag(hashtag, pageable);

        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).should().findByHashtag(hashtag, pageable);
    }

    @DisplayName("Look up Hashtag -> return unique hashtag list")
    @Test
    void lookupHashtagReturnUniqueHashtagList() {

        List<String> hashtags = List.of("#java", "#spring", "#boot");
        given(articleRepository.findAllDistinctHashtags()).willReturn(hashtags);

        List<String> actualHashtags = articleService.getHashtags();

        assertThat(actualHashtags).isEqualTo(hashtags);
        then(articleRepository).should().findAllDistinctHashtags();
    }

    @DisplayName("Give Article Information -> Create Article")
    @Test
    void giveArticleInfoCreateArticle() {

        ArticleDto articleDto = createArticleDto();
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());

        articleService.saveArticle(articleDto);

        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("Give Modified Article Information -> Update Article")
    @Test
    void giveModifiedArticleInfoUpdateArticle() {

        Article article = createArticle();
        ArticleDto articleDto = createArticleDto("new title", "new content", "#spring");
        given(articleRepository.getReferenceById(articleDto.id())).willReturn(article);

        articleService.updateArticle(articleDto.id(), articleDto);

        assertThat(article)
                .hasFieldOrPropertyWithValue("title", articleDto.title())
                .hasFieldOrPropertyWithValue("content", articleDto.content())
                .hasFieldOrPropertyWithValue("hashtag", articleDto.hashtag());
        then(articleRepository).should().getReferenceById(articleDto.id());
    }

    @DisplayName("Give Nonexistent Article Information -> Give Warning Log Do Nothing")
    @Test
    void givenNonexistentArticleInfoLogNothing() {

        ArticleDto articleDto = createArticleDto("title", "content", "#spring");
        given(articleRepository.getReferenceById(articleDto.id())).willThrow(EntityNotFoundException.class);

        articleService.updateArticle(articleDto.id(), articleDto);

        then(articleRepository).should().getReferenceById(articleDto.id());
    }

    @DisplayName("Give Article ID -> Delete Article")
    @Test
    void giveArticleIdDeleteArticle() {

        Long articleId = 1L;
        String userId = "kim";
        willDoNothing().given(articleRepository).deleteByIdAndUserAccount_UserId(articleId, userId);

        articleService.deleteArticle(1L, userId);

        then(articleRepository).should().deleteByIdAndUserAccount_UserId(articleId, userId);
    }


    /**********************************************/
    /********** Private Methods for Test **********/
    /**********************************************/

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "kimbos",
                "password",
                "kimbos0523@gmail.com",
                "kimbos",
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

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "#java");
    }

    private ArticleDto createArticleDto(String title, String content, String hashtag) {
        return ArticleDto.of(1L,
                createUserAccountDto(),
                title,
                content,
                hashtag,
                LocalDateTime.now(),
                "kimbos",
                LocalDateTime.now(),
                "kimbos");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "kimbos",
                "password",
                "kimbos0523@gmail.com",
                "kimbos",
                "memo",
                LocalDateTime.now(),
                "kim",
                LocalDateTime.now(),
                "kim"
        );
    }
}