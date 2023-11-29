package com.kimbos.onlinecommunity.service;

import com.kimbos.onlinecommunity.domain.Article;
import com.kimbos.onlinecommunity.domain.UserAccount;
import com.kimbos.onlinecommunity.domain.enums.SearchType;
import com.kimbos.onlinecommunity.dto.ArticleDto;
import com.kimbos.onlinecommunity.dto.ArticleWithCommentsDto;
import com.kimbos.onlinecommunity.repository.ArticleRepository;
import com.kimbos.onlinecommunity.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserAccountRepository userAccountRepository;


    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        if (searchKeyword == null || searchKeyword.isBlank()) {
            return articleRepository.findAll(pageable).map(ArticleDto::from);
        }

        return switch (searchType) {
            case TITLE -> articleRepository.findByTitleContaining(searchKeyword, pageable).map(ArticleDto::from);
            case CONTENT -> articleRepository.findByContentContaining(searchKeyword, pageable).map(ArticleDto::from);
            case ID -> articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable).map(ArticleDto::from);
            case NICKNAME -> articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(ArticleDto::from);
            case HASHTAG -> articleRepository.findByHashtag("#" + searchKeyword, pageable).map(ArticleDto::from);
        };
    }

    public void saveArticle(ArticleDto articleDto) {
        UserAccount userAccount = userAccountRepository.getReferenceById(articleDto.userAccountDto().userId());
        articleRepository.save(articleDto.toEntity(userAccount));
    }

    public void updateArticle(Long articleId, ArticleDto articleDto) {
        try {
            Article article = articleRepository.getReferenceById(articleId);
            if (articleDto.title() != null) { article.setTitle(articleDto.title()); }
            if (articleDto.content() != null) { article.setContent(articleDto.content()); }
            article.setHashtag(articleDto.hashtag());
        } catch (EntityNotFoundException e) {
            log.warn("Article Update Failure: There is no article match - dto: {}", articleDto);
        }
    }

    public void deleteArticle(long articleId, String userId) {
        articleRepository.deleteByIdAndUserAccount_UserId(articleId, userId);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticlesByHashtag(String hashtag, Pageable pageable) {
        if (hashtag == null || hashtag.isBlank()) {
            return Page.empty(pageable);
        }

        return articleRepository.findByHashtag(hashtag, pageable).map(ArticleDto::from);
    }

    public List<String> getHashtags() {
        return articleRepository.findAllDistinctHashtags();
    }

    @Transactional(readOnly = true)
    public ArticleDto getArticle(Long articleId) {
        return articleRepository.findById(articleId)
                .map(ArticleDto::from)
                .orElseThrow(() -> new EntityNotFoundException("There is no article - articleId: " + articleId));
    }

    public ArticleWithCommentsDto getArticleWithComments(Long articleId) {
        return articleRepository.findById(articleId)
                .map(ArticleWithCommentsDto::from)
                .orElseThrow(() -> new EntityNotFoundException("There is no article - articleId: " + articleId));
    }

    public long getArticleCount() {
        return articleRepository.count();
    }
}
