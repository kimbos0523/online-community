package com.kimbos.onlinecommunity.service;

import com.kimbos.onlinecommunity.domain.Article;
import com.kimbos.onlinecommunity.domain.Hashtag;
import com.kimbos.onlinecommunity.domain.UserAccount;
import com.kimbos.onlinecommunity.domain.enums.SearchType;
import com.kimbos.onlinecommunity.dto.ArticleDto;
import com.kimbos.onlinecommunity.dto.ArticleWithCommentsDto;
import com.kimbos.onlinecommunity.dto.HashtagDto;
import com.kimbos.onlinecommunity.dto.UserAccountDto;
import com.kimbos.onlinecommunity.repository.ArticleRepository;
import com.kimbos.onlinecommunity.repository.HashtagRepository;
import com.kimbos.onlinecommunity.repository.UserAccountRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.as;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("Business Logic - Article")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks private ArticleService articleService;

    @Mock private HashtagService hashtagService;
    @Mock private ArticleRepository articleRepository;
    @Mock private UserAccountRepository userAccountRepository;
    @Mock private HashtagRepository hashtagRepository;

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
                .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags()
                        .stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toUnmodifiableSet()));

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

    @DisplayName("Search Article by Nonexistent hashtag return empty page")
    @Test
    void givenNonexistentHashtag_whenSearchingArticlesViaHashtag_thenReturnsEmptyPage() {
        // Given
        String hashtagName = "Nothing";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtagNames(List.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        // When
        Page<ArticleDto> articles = articleService.searchArticlesByHashtag(hashtagName, pageable);

        // Then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
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

        String hashtagName = "#java spring";
        Pageable pageable = Pageable.ofSize(20);
        Article expectedArticle = createArticle();
        given(articleRepository.findByHashtagNames(List.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(expectedArticle), pageable, 1));

        Page<ArticleDto> articles = articleService.searchArticlesByHashtag(hashtagName, pageable);

        assertThat(articles).isEqualTo(new PageImpl<>(List.of(ArticleDto.from(expectedArticle)), pageable, 1));
        then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
    }

    @DisplayName("Look up Hashtag -> return unique hashtag list")
    @Test
    void lookupHashtagReturnUniqueHashtagList() {

        Article article = createArticle();
        List<String> hashtags = List.of("#java", "#spring", "#boot");
        given(hashtagRepository.findAllHashtagNames()).willReturn(hashtags);

        List<String> actualHashtags = articleService.getHashtags();

        assertThat(actualHashtags).isEqualTo(hashtags);
        then(hashtagRepository).should().findAllHashtagNames();
    }

    @DisplayName("Give Article Information -> Create Article")
    @Test
    void giveArticleInfoCreateArticle() {

        ArticleDto articleDto = createArticleDto();
        Set<String> expectedHashtagNames = Set.of("java", "spring");
        Set<Hashtag> expectedHashtags = new HashSet<>();
        expectedHashtags.add(createHashtag("java"));

        given(userAccountRepository.getReferenceById(articleDto.userAccountDto().userId())).willReturn(createUserAccount());
        given(hashtagService.parseHashtagNames(articleDto.content())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());

        articleService.saveArticle(articleDto);

        then(userAccountRepository).should().getReferenceById(articleDto.userAccountDto().userId());
        then(hashtagService).should().parseHashtagNames(articleDto.content());
        then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("Give Modified Article Information -> Update Article")
    @Test
    void giveModifiedArticleInfoUpdateArticle() {

        Article article = createArticle();
        ArticleDto articleDto = createArticleDto("New title", "New content #springboot");
        Set<String> expectedHashtagNames = Set.of("springboot");
        Set<Hashtag> expectedHashtags = new HashSet<>();

        given(articleRepository.getReferenceById(articleDto.id())).willReturn(article);
        given(userAccountRepository.getReferenceById(articleDto.userAccountDto().userId())).willReturn(articleDto.userAccountDto().toEntity());
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());
        given(hashtagService.parseHashtagNames(articleDto.content())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);

        articleService.updateArticle(articleDto.id(), articleDto);

        assertThat(article)
                .hasFieldOrPropertyWithValue("title", articleDto.title())
                .hasFieldOrPropertyWithValue("content", articleDto.content())
                .extracting("hashtags", as(InstanceOfAssertFactories.COLLECTION))
                .hasSize(1)
                .extracting("hashtagName")
                .containsExactly("springboot");
        then(articleRepository).should().getReferenceById(articleDto.id());
        then(articleRepository).should().flush();
        then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
        then(hashtagService).should().parseHashtagNames(articleDto.content());
        then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
    }

    @DisplayName("Give Nonexistent Article Information -> Give Warning Log Do Nothing")
    @Test
    void givenNonexistentArticleInfoLogNothing() {

        ArticleDto articleDto = createArticleDto("title", "content");
        given(articleRepository.getReferenceById(articleDto.id())).willThrow(EntityNotFoundException.class);

        articleService.updateArticle(articleDto.id(), articleDto);

        then(articleRepository).should().getReferenceById(articleDto.id());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(hashtagService).shouldHaveNoInteractions();
    }

    @DisplayName("Give Article ID -> Delete Article")
    @Test
    void giveArticleIdDeleteArticle() {

        Long articleId = 1L;
        String userId = "kim";
        given(articleRepository.getReferenceById(articleId)).willReturn(createArticle());
        willDoNothing().given(articleRepository).deleteByIdAndUserAccount_UserId(articleId, userId);
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());

        articleService.deleteArticle(1L, userId);

        then(articleRepository).should().getReferenceById(articleId);
        then(articleRepository).should().deleteByIdAndUserAccount_UserId(articleId, userId);
        then(articleRepository).should().flush();
        then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
    }


    /**********************************************/
    /********** Private Methods for Test **********/
    /**********************************************/

    private UserAccount createUserAccount() {
        return createUserAccount("kim");
    }
    private UserAccount createUserAccount(String userId) {
        return UserAccount.of(
                userId,
                "password",
                "kimbos0523@gmail.com",
                "kim",
                null
        );
    }

    private Article createArticle() {
        return createArticle(1L);
    }

    private Article createArticle(Long id) {
        Article article = Article.of(
                createUserAccount(),
                "title",
                "content"
        );

        article.addHashtags(Set.of(
                createHashtag(1L, "java"),
                createHashtag(2L, "spring")
        ));

        ReflectionTestUtils.setField(article, "id", id);

        return article;
    }

    private Hashtag createHashtag(String hashtagName) {
        return createHashtag(1L, hashtagName);
    }

    private Hashtag createHashtag(Long id, String hashtagName) {
        Hashtag hashtag = Hashtag.of(hashtagName);
        ReflectionTestUtils.setField(hashtag, "id", id);

        return hashtag;
    }

    private HashtagDto createHashtagDto() {
        return HashtagDto.of("java");
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content");
    }

    private ArticleDto createArticleDto(String title, String content) {
        return ArticleDto.of(1L,
                createUserAccountDto(),
                title,
                content,
                null,
                LocalDateTime.now(),
                "kim",
                LocalDateTime.now(),
                "kim");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "kim",
                "password",
                "kimbos0523@gmail.com",
                "kim",
                "memo",
                LocalDateTime.now(),
                "kim",
                LocalDateTime.now(),
                "kim"
        );
    }
}