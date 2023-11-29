package com.kimbos.onlinecommunity.controller;

import com.kimbos.onlinecommunity.domain.enums.FormStatus;
import com.kimbos.onlinecommunity.domain.enums.SearchType;
import com.kimbos.onlinecommunity.dto.ArticleRequest;
import com.kimbos.onlinecommunity.dto.ArticleResponse;
import com.kimbos.onlinecommunity.dto.ArticleWithCommentsResponse;
import com.kimbos.onlinecommunity.dto.UserAccountDto;
import com.kimbos.onlinecommunity.dto.security.CommunityPrincipal;
import com.kimbos.onlinecommunity.service.ArticleService;
import com.kimbos.onlinecommunity.service.PaginationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/articles")
@Controller
public class ArticleController {

    private final ArticleService articleService;
    private final PaginationService paginationService;

    @GetMapping
    public String articles(
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map
    ) {
        Page<ArticleResponse> articles = articleService.searchArticles(searchType, searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNums = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());

        map.addAttribute("articles", articles);
        map.addAttribute("paginationBarNumbers", barNums);
        map.addAttribute("searchTypes", SearchType.values());

        return "articles/index";
    }

    @GetMapping("/{articleId}")
    public String article(@PathVariable Long articleId, ModelMap map) {
        ArticleWithCommentsResponse article = ArticleWithCommentsResponse.from(articleService.getArticleWithComments(articleId));
        map.addAttribute("article", article);
        map.addAttribute("comments", article.commentsResponse());
        map.addAttribute("totalCount", articleService.getArticleCount());

        return "articles/detail";
    }

    @GetMapping("/search-hashtag")
    public String searchHashtag(
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map
    ) {
        Page<ArticleResponse> articles = articleService.searchArticlesByHashtag(searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNums = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());
        List<String> hashtags = articleService.getHashtags();

        map.addAttribute("articles", articles);
        map.addAttribute("paginationBarNumbers", barNums);
        map.addAttribute("hashtags", hashtags);
        map.addAttribute("searchType", SearchType.HASHTAG);

        return "articles/search-hashtag";
    }

    @GetMapping("/form")
    public String articleForm(ModelMap map) {
        map.addAttribute("formStatus", FormStatus.CREATE);

        return "articles/form";
    }

    @PostMapping ("/form")
    public String postNewArticle(ArticleRequest articleRequest) {
        // TODO: Authentication
        articleService.saveArticle(articleRequest.toDto(UserAccountDto.of(
                "kim", "asdf1234", "kimbos0523@gmail.com", "kim", "test memo", null, null, null, null
        )));

        return "redirect:/articles";
    }

    @GetMapping("/{articleId}/form")
    public String updateArticleForm(
            @PathVariable Long articleId,
            ModelMap map
    ) {
        ArticleResponse article = ArticleResponse.from(articleService.getArticle(articleId));

        map.addAttribute("article", article);
        map.addAttribute("formStatus", FormStatus.UPDATE);

        return "articles/form";
    }

    @PostMapping ("/{articleId}/form")
    public String updateArticle(@PathVariable Long articleId, ArticleRequest articleRequest) {
        // TODO: Authentication
        articleService.updateArticle(articleId, articleRequest.toDto(UserAccountDto.of(
                "kim", "asdf1234", "kimbos0523@gmail.com", "kim", "test memo")));

        return "redirect:/articles/" + articleId;
    }

    @PostMapping ("/{articleId}/delete")
    public String deleteArticle(
            @PathVariable Long articleId,
            @AuthenticationPrincipal CommunityPrincipal communityPrincipal
    ) {
        articleService.deleteArticle(articleId, communityPrincipal.getUsername());

        return "redirect:/articles";
    }
}
