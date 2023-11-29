package com.kimbos.onlinecommunity.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Business Logic - Pagination")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = PaginationService.class)
class PaginationServiceTest {

    private final PaginationService paginationService;

    public PaginationServiceTest(@Autowired PaginationService paginationService) {
        this.paginationService = paginationService;
    }

    @DisplayName("Current Page number, Total Page number -> Make Paging Bar List")
    @MethodSource
    @ParameterizedTest(name = "[{index}] Current Page: {0}, Total Page: {1} => {2}")
    void curPageNumTotalPageNumMakePagingBarList(int curPageNum, int totalPages, List<Integer> expected) {


        List<Integer> actual = paginationService.getPaginationBarNumbers(curPageNum, totalPages);

        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> curPageNumTotalPageNumMakePagingBarList() {
        return Stream.of(
                arguments(0, 13, List.of(0, 1, 2, 3, 4)),
                arguments(1, 13, List.of(0, 1, 2, 3, 4)),
                arguments(2, 13, List.of(0, 1, 2, 3, 4)),
                arguments(3, 13, List.of(1, 2, 3, 4, 5)),
                arguments(4, 13, List.of(2, 3, 4, 5, 6)),
                arguments(5, 13, List.of(3, 4, 5, 6, 7)),
                arguments(10, 13, List.of(8, 9, 10, 11, 12)),
                arguments(11, 13, List.of(9, 10, 11, 12)),
                arguments(12, 13, List.of(10, 11, 12))
        );
    }

    @DisplayName("Inform the setting Pagination Bar Length")
    @Test
    void informSettingPaginationBarLength() {

        int barLength = paginationService.currentBarLength();

        assertThat(barLength).isEqualTo(5);
    }

}