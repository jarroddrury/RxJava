/**
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.operators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import rx.IObservable;
import rx.Observable;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

/**
 * Returns the minimum element in an observable sequence.
 */
public class OperationMinMax {

    public static <T extends Comparable<? super T>> Observable<T> min(
            IObservable<T> source) {
        return minMax(source, -1L);
    }

    public static <T> Observable<T> min(IObservable<T> source,
            final Comparator<? super T> comparator) {
        return minMax(source, comparator, -1L);
    }

    public static <T, R extends Comparable<? super R>> Observable<List<T>> minBy(
            IObservable<T> source, final Func1<T, R> selector) {
        return minMaxBy(source, selector, -1L);
    }

    public static <T, R> Observable<List<T>> minBy(IObservable<T> source,
            final Func1<T, R> selector, final Comparator<? super R> comparator) {
        return minMaxBy(source, selector, comparator, -1L);
    }

    public static <T extends Comparable<? super T>> Observable<T> max(
            IObservable<T> source) {
        return minMax(source, 1L);
    }

    public static <T> Observable<T> max(IObservable<T> source,
            final Comparator<? super T> comparator) {
        return minMax(source, comparator, 1L);
    }

    public static <T, R extends Comparable<? super R>> Observable<List<T>> maxBy(
            IObservable<T> source, final Func1<T, R> selector) {
        return minMaxBy(source, selector, 1L);
    }

    public static <T, R> Observable<List<T>> maxBy(IObservable<T> source,
            final Func1<T, R> selector, final Comparator<? super R> comparator) {
        return minMaxBy(source, selector, comparator, 1L);
    }

    private static <T extends Comparable<? super T>> Observable<T> minMax(
            IObservable<T> isource, final long flag) {
        final Observable<T> source = Observable.from(isource);
        return source.reduce(new Func2<T, T, T>() {
            @Override
            public T call(T acc, T value) {
                if (flag * acc.compareTo(value) > 0) {
                    return acc;
                }
                return value;
            }
        });
    }

    private static <T> Observable<T> minMax(IObservable<T> isource,
            final Comparator<? super T> comparator, final long flag) {
        final Observable<T> source = Observable.from(isource);
        return source.reduce(new Func2<T, T, T>() {
            @Override
            public T call(T acc, T value) {
                if (flag * comparator.compare(acc, value) > 0) {
                    return acc;
                }
                return value;
            }
        });
    }

    private static <T, R extends Comparable<? super R>> Observable<List<T>> minMaxBy(
            IObservable<T> isource, final Func1<T, R> selector, final long flag) {
        final Observable<T> source = Observable.from(isource);
        return source.reduce(new ArrayList<T>(),
                new Func2<List<T>, T, List<T>>() {

                    @Override
                    public List<T> call(List<T> acc, T value) {
                        if (acc.isEmpty()) {
                            acc.add(value);
                        } else {
                            int compareResult = selector.call(acc.get(0))
                                    .compareTo(selector.call(value));
                            if (compareResult == 0) {
                                acc.add(value);
                            } else if (flag * compareResult < 0) {
                                acc.clear();
                                acc.add(value);
                            }
                        }
                        return acc;
                    }
                });
    }

    private static <T, R> Observable<List<T>> minMaxBy(IObservable<T> isource,
            final Func1<T, R> selector, final Comparator<? super R> comparator,
            final long flag) {
        final Observable<T> source = Observable.from(isource);
        return source.reduce(new ArrayList<T>(),
                new Func2<List<T>, T, List<T>>() {

                    @Override
                    public List<T> call(List<T> acc, T value) {
                        if (acc.isEmpty()) {
                            acc.add(value);
                        } else {
                            int compareResult = comparator.compare(
                                    selector.call(acc.get(0)),
                                    selector.call(value));
                            if (compareResult == 0) {
                                acc.add(value);
                            } else if (flag * compareResult < 0) {
                                acc.clear();
                                acc.add(value);
                            }
                        }
                        return acc;
                    }
                });
    }

}
