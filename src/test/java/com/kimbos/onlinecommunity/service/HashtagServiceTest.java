package com.kimbos.onlinecommunity.service;

import com.kimbos.onlinecommunity.domain.Hashtag;
import com.kimbos.onlinecommunity.repository.HashtagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Business Logic - Hashtag")
@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @InjectMocks
    private HashtagService hashtagService;

    @Mock
    private HashtagRepository hashtagRepository;

    @DisplayName("Parsing content -> return unique hashtag names.")
    @MethodSource
    @ParameterizedTest(name = "[{index}] \"{0}\" => {1}")
    void parsingContentReturnUniqueHashtagNames(String input, Set<String> expected) {

        Set<String> actual = hashtagService.parseHashtagNames(input);
        
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        then(hashtagRepository).shouldHaveNoInteractions();
    }

    static Stream<Arguments> parsingContentReturnUniqueHashtagNames() {
        return Stream.of(
                arguments(null, Set.of()),
                arguments("", Set.of()),
                arguments("   ", Set.of()),
                arguments("#", Set.of()),
                arguments("  #", Set.of()),
                arguments("#   ", Set.of()),
                arguments("java", Set.of()),
                arguments("java#", Set.of()),
                arguments("ja#va", Set.of("va")),
                arguments("#java", Set.of("java")),
                arguments("#java_spring", Set.of("java_spring")),
                arguments("#java-spring", Set.of("java")),
                arguments("#_java_spring", Set.of("_java_spring")),
                arguments("#-java-spring", Set.of()),
                arguments("#_java_spring__", Set.of("_java_spring__")),
                arguments("#java#spring", Set.of("java", "spring")),
                arguments("#java #spring", Set.of("java", "spring")),
                arguments("#java  #spring", Set.of("java", "spring")),
                arguments("#java   #spring", Set.of("java", "spring")),
                arguments("#java     #spring", Set.of("java", "spring")),
                arguments("  #java     #spring ", Set.of("java", "spring")),
                arguments("   #java     #spring   ", Set.of("java", "spring")),
                arguments("#java#spring#boot", Set.of("java", "spring", "boot")),
                arguments("#java #spring#boot", Set.of("java", "spring", "boot")),
                arguments("#java#spring #boot", Set.of("java", "spring", "boot")),
                arguments("#java,#spring,#boot", Set.of("java", "spring", "boot")),
                arguments("#java.#spring;#boot", Set.of("java", "spring", "boot")),
                arguments("#java|#spring:#boot", Set.of("java", "spring", "boot")),
                arguments("#java #spring  #boot", Set.of("java", "spring", "boot")),
                arguments("   #java,? #spring  ...  #boot ", Set.of("java", "spring", "boot")),
                arguments("#java#java#spring#boot", Set.of("java", "spring", "boot")),
                arguments("#java#java#java#spring#boot", Set.of("java", "spring", "boot")),
                arguments("#java#spring#java#boot#java", Set.of("java", "spring", "boot")),
                arguments("#java#spring long text~~~~~~~~~~~~~~~~~~~~~", Set.of("java", "spring")),
                arguments("long text~~~~~~~~~~~~~~~~~~~~~#java#spring", Set.of("java", "spring")),
                arguments("long text~~~~~~#java#spring~~~~~~~~~~~~~~~", Set.of("java", "spring")),
                arguments("long text~~~~~~#java~~~~~~~#spring~~~~~~~~", Set.of("java", "spring"))
        );
    }

    @DisplayName("Input hashtag names -> return hashtag that match in saving without duplicated.")
    @Test
    void inputHashtagNamesReturnHashtagSet() {
        
        Set<String> hashtagNames = Set.of("java", "spring", "boots");
        given(hashtagRepository.findByHashtagNameIn(hashtagNames)).willReturn(List.of(
                Hashtag.of("java"),
                Hashtag.of("spring")
        ));
        
        Set<Hashtag> hashtags = hashtagService.findHashtagsByNames(hashtagNames);
        
        assertThat(hashtags).hasSize(2);
        then(hashtagRepository).should().findByHashtagNameIn(hashtagNames);
    }
}