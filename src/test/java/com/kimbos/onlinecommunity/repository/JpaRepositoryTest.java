package com.kimbos.onlinecommunity.repository;

import com.kimbos.onlinecommunity.config.JpaConfig;
import com.kimbos.onlinecommunity.domain.Article;
import com.kimbos.onlinecommunity.domain.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Test for JPA")
@Import({JpaRepositoryTest.TestJpaConfig.class})
@DataJpaTest
class JpaRepositoryTest {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserAccountRepository userAccountRepository;

    public JpaRepositoryTest(
            @Autowired ArticleRepository articleRepository,
            @Autowired CommentRepository commentRepository,
            @Autowired UserAccountRepository userAccountRepository
    ) {
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @DisplayName("Select Test")
    @Test
    void jpaSelectTest() {

        List<Article> articles = articleRepository.findAll();

        assertThat(articles).isNotNull().hasSize(123);
    }

    @DisplayName("Insert Test")
    @Test
    void jpaInsertTest() {

        long previousCount = articleRepository.count();
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("newUno", "pw", null, null, null));
        Article article = Article.of(userAccount, "new article", "new content", "#spring");

        articleRepository.save(article);

        assertThat(articleRepository.count()).isEqualTo(previousCount + 1);
    }

    @DisplayName("Update Test")
    @Test
    void jpaUpdateTest() {

        Article article = articleRepository.findById(1L).orElseThrow();
        String updatedHashtag = "#springboot";
        article.setHashtag(updatedHashtag);

        Article savedArticle = articleRepository.saveAndFlush(article);

        assertThat(savedArticle).hasFieldOrPropertyWithValue("hashtag", updatedHashtag);
    }

    @DisplayName("Delete Test")
    @Test
    void jpaDeleteTest() {

        Article article = articleRepository.findById(1L).orElseThrow();
        long previousArticleCount = articleRepository.count();
        long previousCommentCount = commentRepository.count();
        int deletedCommentsSize = article.getComments().size();

        articleRepository.delete(article);

        assertThat(articleRepository.count()).isEqualTo(previousArticleCount - 1);
        assertThat(commentRepository.count()).isEqualTo(previousCommentCount - deletedCommentsSize);
    }

    @EnableJpaAuditing
    @TestConfiguration
    public static class TestJpaConfig {
        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("kim");
        }
    }
}