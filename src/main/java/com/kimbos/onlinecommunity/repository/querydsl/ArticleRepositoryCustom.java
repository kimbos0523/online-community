package com.kimbos.onlinecommunity.repository.querydsl;

import java.util.List;

public interface ArticleRepositoryCustom {

    List<String> findAllDistinctHashtags();
}
