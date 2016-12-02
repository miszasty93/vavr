/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang.collection.euler;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.CharSeq;
import javaslang.collection.List;
import org.junit.Test;

import static javaslang.collection.euler.Utils.factors;
import static org.assertj.core.api.Assertions.assertThat;

public class Euler33Test {

    /**
     * <strong>Problem 33: Digit cancelling fractions</strong>
     * <p>
     * The fraction 49/98 is a curious fraction, as an inexperienced
     * mathematician in attempting to simplify it may incorrectly believe that
     * 49/98 = 4/8, which is correct, is obtained by cancelling the 9s.
     * <p>
     * We shall consider fractions like, 30/50 = 3/5, to be trivial examples.
     * <p>
     * There are exactly four non-trivial examples of this type of fraction,
     * less than one in value, and containing two digits in the numerator and
     * denominator.
     * <p>
     * If the product of these four fractions is given in its lowest common
     * terms, find the value of the denominator.
     * <p>
     * See also <a href="https://projecteuler.net/problem=33">projecteuler.net
     * problem 33</a>.
     */
    @Test
    public void shouldSolveProblem33() {
        assertThat(isNonTrivialDigitCancellingFraction(Tuple.of(49, 98))).isTrue();
        assertThat(isNonTrivialDigitCancellingFraction(Tuple.of(30, 50))).isFalse();
        assertThat(isNonTrivialDigitCancellingFraction(Tuple.of(21, 22))).isFalse();
        assertThat(lowestCommonDenominatorOfProductOfTheFourNonTrivialdigitCancellingFractions()).isEqualTo(100);
    }

    private static int lowestCommonDenominatorOfProductOfTheFourNonTrivialdigitCancellingFractions() {
        return List.rangeClosed(10, 98)
                .flatMap(n -> List.rangeClosed(n + 1, 99).map(d -> Tuple.of(n, d)))
                .filter(Euler33Test::isNonTrivialDigitCancellingFraction)
                .fold(Tuple.of(1, 1), Euler33Test::multiplyFractions)
                .apply(Euler33Test::simplifyFraction)._2;
    }

    private static boolean isNonTrivialDigitCancellingFraction(Tuple2<Integer, Integer> fraction) {
        return CharSeq.of(fraction._1.toString())
                .filter(d -> d != '0')
                .find(d -> CharSeq.of(fraction._2.toString()).contains(d))
                .map(d -> fractionCanBeSimplifiedByCancellingDigit(fraction, d))
                .getOrElse(false);
    }

    private static boolean fractionCanBeSimplifiedByCancellingDigit(Tuple2<Integer, Integer> fraction, char d) {
        return Tuple.of(CharSeq.of(fraction._1.toString()).remove(d), CharSeq.of(fraction._2.toString()).remove(d))
                .map(CharSeq::mkString, CharSeq::mkString)
                .map(Double::valueOf, Double::valueOf)
                .apply((d1, d2) -> fraction._1 / d1 == fraction._2 / d2);
    }

    private static Tuple2<Integer, Integer> multiplyFractions(Tuple2<Integer, Integer> f1, Tuple2<Integer, Integer> f2) {
        return Tuple.of(f1._1 * f2._1, f1._2 * f2._2);
    }

    private static Tuple2<Integer, Integer> simplifyFraction(int numerator, int denominator) {
        return factors(numerator)
                .map(Long::intValue)
                .filter(f -> f != 1)
                .sorted()
                .findLast(f -> denominator % f == 0)
                .map(f -> simplifyFraction(numerator / f, denominator / f))
                .getOrElse(Tuple.of(numerator, denominator));
    }
}
