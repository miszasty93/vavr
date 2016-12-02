/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang.collection;

import javaslang.JmhRunner;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Objects;

import static java.util.Arrays.asList;
import static javaslang.JmhRunner.create;
import static javaslang.JmhRunner.getRandomValues;
import static javaslang.collection.Collections.areEqual;

public class ArrayBenchmark {
    static final Array<Class<?>> CLASSES = Array.of(
            Create.class,
            Head.class,
            Tail.class,
            Get.class,
            Update.class,
            Prepend.class,
            Append.class,
            Iterate.class
    );

    @Test
    public void testAsserts() {
        JmhRunner.runDebugWithAsserts(CLASSES);
    }

    public static void main(String... args) {
        JmhRunner.runNormalNoAsserts(CLASSES);
    }

    @State(Scope.Benchmark)
    public static class Base {
        @Param({ "10", "100", "1000" })
        public int CONTAINER_SIZE;

        int EXPECTED_AGGREGATE;
        Integer[] ELEMENTS;

        java.util.ArrayList<Integer> javaMutable;
        fj.data.Array<Integer> fjavaMutable;
        javaslang.collection.Array<Integer> slangPersistent;

        @Setup
        public void setup() {
            ELEMENTS = getRandomValues(CONTAINER_SIZE, 0);
            EXPECTED_AGGREGATE = Iterator.of(ELEMENTS).reduce(JmhRunner::aggregate);

            javaMutable = create(java.util.ArrayList::new, asList(ELEMENTS), v -> areEqual(v, asList(ELEMENTS)));
            fjavaMutable = create(fj.data.Array::array, ELEMENTS, ELEMENTS.length, v -> areEqual(v, asList(ELEMENTS)));
            slangPersistent = create(javaslang.collection.Array::ofAll, javaMutable, v -> areEqual(v, javaMutable));
        }
    }

    public static class Create extends Base {
        @Benchmark
        public Object java_mutable() {
            final ArrayList<Integer> values = new ArrayList<>(javaMutable);
            assert areEqual(values, javaMutable);
            return values;
        }

        @Benchmark
        public Object fjava_persistent() {
            final fj.data.Array<Integer> values = fj.data.Array.iterableArray(javaMutable);
            assert areEqual(values, fjavaMutable);
            return values;
        }

        @Benchmark
        public Object slang_persistent() {
            final javaslang.collection.Array<Integer> values = javaslang.collection.Array.ofAll(javaMutable);
            assert areEqual(values, slangPersistent);
            return values.head();
        }
    }

    public static class Head extends Base {
        @Benchmark
        public Object java_mutable() {
            final Object head = javaMutable.get(0);
            assert Objects.equals(head, ELEMENTS[0]);
            return head;
        }

        @Benchmark
        public Object fjava_mutable() {
            final Object head = fjavaMutable.get(0);
            assert Objects.equals(head, ELEMENTS[0]);
            return head;
        }

        @Benchmark
        public Object slang_persistent() {
            final Object head = slangPersistent.get(0);
            assert Objects.equals(head, ELEMENTS[0]);
            return head;
        }
    }

    @SuppressWarnings("Convert2MethodRef")
    public static class Tail extends Base {
        @State(Scope.Thread)
        public static class Initialized {
            final java.util.ArrayList<Integer> javaMutable = new java.util.ArrayList<>();

            @Setup(Level.Invocation)
            public void initializeMutable(Base state) {
                java.util.Collections.addAll(javaMutable, state.ELEMENTS);
                assert areEqual(javaMutable, asList(state.ELEMENTS));
            }

            @TearDown(Level.Invocation)
            public void tearDown() {
                javaMutable.clear();
            }
        }

        @Benchmark
        public Object java_mutable(Initialized state) {
            final java.util.ArrayList<Integer> values = state.javaMutable;
            for (int i = 0; i < CONTAINER_SIZE; i++) {
                values.remove(0);
            }
            assert values.isEmpty();
            return values;
        }

        @Benchmark
        public Object slang_persistent() {
            javaslang.collection.Array<Integer> values = slangPersistent;
            for (int i = 0; i < CONTAINER_SIZE; i++) {
                values = values.tail();
            }
            assert values.isEmpty();
            return values;
        }
    }

    public static class Get extends Base {
        @Benchmark
        public int java_mutable() {
            int aggregate = 0;
            for (int i = 0; i < CONTAINER_SIZE; i++) {
                aggregate ^= javaMutable.get(i);
            }
            assert aggregate == EXPECTED_AGGREGATE;
            return aggregate;
        }

        @Benchmark
        public int fjava_mutable() {
            int aggregate = 0;
            for (int i = 0; i < ELEMENTS.length; i++) {
                aggregate ^= fjavaMutable.get(i);
            }
            assert aggregate == EXPECTED_AGGREGATE;
            return aggregate;
        }

        @Benchmark
        public int slang_persistent() {
            int aggregate = 0;
            for (int i = 0; i < CONTAINER_SIZE; i++) {
                aggregate ^= slangPersistent.get(i);
            }
            assert aggregate == EXPECTED_AGGREGATE;
            return aggregate;
        }
    }

    public static class Update extends Base {
        @Benchmark
        public Object java_mutable() {
            final java.util.ArrayList<Integer> values = javaMutable;
            for (int i = 0; i < CONTAINER_SIZE; i++) {
                values.set(i, 0);
            }
            assert Iterator.ofAll(values).forAll(e -> e == 0);
            return javaMutable;
        }

        @Benchmark
        public Object fjava_mutable() {
            final fj.data.Array<Integer> values = fjavaMutable;
            for (int i = 0; i < ELEMENTS.length; i++) {
                values.set(i, 0);
            }
            assert values.forall(e -> e == 0);
            return fjavaMutable;
        }

        @Benchmark
        public Object slang_persistent() {
            javaslang.collection.Array<Integer> values = slangPersistent;
            for (int i = 0; i < CONTAINER_SIZE; i++) {
                values = values.update(i, 0);
            }
            assert values.forAll(e -> e == 0);
            return values;
        }
    }

    public static class Prepend extends Base {
        @Benchmark
        public Object java_mutable() {
            final java.util.ArrayList<Integer> values = new java.util.ArrayList<>(CONTAINER_SIZE);
            for (Integer element : ELEMENTS) {
                values.add(0, element);
            }
            assert areEqual(List.ofAll(values).reverse(), javaMutable);
            return values;
        }

        @Benchmark
        public Object fjava_mutable() {
            fj.data.Array<Integer> values = fj.data.Array.empty();
            for (Integer element : ELEMENTS) {
                values = fj.data.Array.array(element).append(values);
            }
            assert areEqual(values.reverse(), javaMutable);
            return values;
        }

        @Benchmark
        public Object slang_persistent() {
            javaslang.collection.Array<Integer> values = javaslang.collection.Array.empty();
            for (Integer element : ELEMENTS) {
                values = values.prepend(element);
            }
            assert areEqual(values.reverse(), javaMutable);
            return values;
        }
    }

    public static class Append extends Base {
        @SuppressWarnings("ManualArrayToCollectionCopy")
        @Benchmark
        public Object java_mutable() {
            final java.util.ArrayList<Integer> values = new java.util.ArrayList<>(CONTAINER_SIZE);
            for (Integer element : ELEMENTS) {
                values.add(element);
            }
            assert areEqual(values, javaMutable);
            return values;
        }

        @Benchmark
        public Object fjava_mutable() {
            fj.data.Array<Integer> values = fj.data.Array.empty();
            for (Integer element : ELEMENTS) {
                values = values.append(fj.data.Array.array(element));
            }
            assert areEqual(values, javaMutable);
            return values;
        }

        @Benchmark
        public Object slang_persistent() {
            javaslang.collection.Array<Integer> values = javaslang.collection.Array.empty();
            for (Integer element : ELEMENTS) {
                values = values.append(element);
            }
            assert areEqual(values, javaMutable);
            return values;
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static class Iterate extends Base {
        @Benchmark
        public int java_mutable() {
            int aggregate = 0;
            for (int i = 0; i < CONTAINER_SIZE; i++) {
                aggregate ^= javaMutable.get(i);
            }
            assert aggregate == EXPECTED_AGGREGATE;
            return aggregate;
        }

        @Benchmark
        public int fjava_mutable() {
            int aggregate = 0;
            for (final java.util.Iterator<Integer> iterator = fjavaMutable.iterator(); iterator.hasNext(); ) {
                aggregate ^= iterator.next();
            }
            assert aggregate == EXPECTED_AGGREGATE;
            return aggregate;
        }

        @Benchmark
        public int slang_persistent() {
            int aggregate = 0;
            for (final Iterator<Integer> iterator = slangPersistent.iterator(); iterator.hasNext(); ) {
                aggregate ^= iterator.next();
            }
            assert aggregate == EXPECTED_AGGREGATE;
            return aggregate;
        }
    }
}