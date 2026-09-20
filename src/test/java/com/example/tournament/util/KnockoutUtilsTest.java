package com.example.tournament.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnockoutUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "2, 2",
            "3, 4",
            "4, 4",
            "5, 8",
            "8, 8",
            "9, 16",
            "10, 16",
            "16, 16",
            "17, 32",
            "24, 32"
    })
    void bracketSize_isNextPowerOfTwo(int teamCount, int expected) {
        assertThat(KnockoutUtils.bracketSize(teamCount)).isEqualTo(expected);
    }

    @Test
    void bracketSize_rejectsZero() {
        assertThatThrownBy(() -> KnockoutUtils.bracketSize(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "8, 3",
            "16, 4",
            "32, 5"
    })
    void numberOfRounds_derivedFromBracketSize(int bracketSize, int expected) {
        assertThat(KnockoutUtils.numberOfRounds(bracketSize)).isEqualTo(expected);
    }

    @Test
    void tenTeams_producesSixByesInSixteenBracket() {
        assertThat(KnockoutUtils.numberPlateau(16, 10)).isEqualTo(6);
    }

    @Test
    void sixteenBracket_roundsHaveCorrectMatchCount() {
        // 1er tour (huitièmes) : 8 matchs ; quarts : 4 ; demis : 2 ; finale : 1
        assertThat(KnockoutUtils.matchesForRound(16, 1)).isEqualTo(8);
        assertThat(KnockoutUtils.matchesForRound(16, 2)).isEqualTo(4);
        assertThat(KnockoutUtils.matchesForRound(16, 3)).isEqualTo(2);
        assertThat(KnockoutUtils.matchesForRound(16, 4)).isEqualTo(1);
    }

    @Test
    void isPowerOfTwo_detectsPowersAndOthers() {
        assertThat(KnockoutUtils.isPowerOfTwo(1)).isTrue();
        assertThat(KnockoutUtils.isPowerOfTwo(2)).isTrue();
        assertThat(KnockoutUtils.isPowerOfTwo(16)).isTrue();
        assertThat(KnockoutUtils.isPowerOfTwo(10)).isFalse();
        assertThat(KnockoutUtils.isPowerOfTwo(0)).isFalse();
    }
}