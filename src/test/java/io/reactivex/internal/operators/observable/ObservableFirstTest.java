/**
 * Copyright 2016 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.internal.operators.observable;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;

import org.junit.*;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.*;
import io.reactivex.functions.Predicate;

public class ObservableFirstTest {

    @Mock
    Observer<String> w;
    @Mock
    SingleObserver<String> s;
    @Mock
    MaybeObserver<String> m;

    private static final Predicate<String> IS_D = new Predicate<String>() {
        @Override
        public boolean test(String value) {
            return "d".equals(value);
        }
    };

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFirstOrElseOfNone() {
        Observable<String> src = Observable.empty();
        src.first("default").subscribe(s);

        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void testFirstOrElseOfSome() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.first("default").subscribe(s);

        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("a");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void testFirstOrElseWithPredicateOfNoneMatchingThePredicate() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.filter(IS_D).first("default").subscribe(s);

        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void testFirstOrElseWithPredicateOfSome() {
        Observable<String> src = Observable.just("a", "b", "c", "d", "e", "f");
        src.filter(IS_D).first("default").subscribe(s);

        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("d");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void testFirst() {
        Maybe<Integer> o = Observable.just(1, 2, 3).first();

        MaybeObserver<Integer> NbpObserver = TestHelper.mockMaybeObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(1);
        inOrder.verify(NbpObserver, never()).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstWithOneElement() {
        Maybe<Integer> o = Observable.just(1).first();

        MaybeObserver<Integer> NbpObserver = TestHelper.mockMaybeObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(1);
        inOrder.verify(NbpObserver, never()).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstWithEmpty() {
        Maybe<Integer> o = Observable.<Integer> empty().first();

        MaybeObserver<Integer> NbpObserver = TestHelper.mockMaybeObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onError(
                isA(NoSuchElementException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstWithPredicate() {
        Maybe<Integer> o = Observable.just(1, 2, 3, 4, 5, 6)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer t1) {
                        return t1 % 2 == 0;
                    }
                })
                .first();

        MaybeObserver<Integer> NbpObserver = TestHelper.mockMaybeObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(2);
        inOrder.verify(NbpObserver, never()).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstWithPredicateAndOneElement() {
        Maybe<Integer> o = Observable.just(1, 2)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer t1) {
                        return t1 % 2 == 0;
                    }
                })
                .first();

        MaybeObserver<Integer> NbpObserver = TestHelper.mockMaybeObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(2);
        inOrder.verify(NbpObserver, never()).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstWithPredicateAndEmpty() {
        Maybe<Integer> o = Observable.just(1)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer t1) {
                        return t1 % 2 == 0;
                    }
                })
                .first();
        
        MaybeObserver<Integer> NbpObserver = TestHelper.mockMaybeObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onError(
                isA(NoSuchElementException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstOrDefault() {
        Single<Integer> o = Observable.just(1, 2, 3)
                .first(4);

        SingleObserver<Integer> NbpObserver = TestHelper.mockSingleObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstOrDefaultWithOneElement() {
        Single<Integer> o = Observable.just(1).first(2);

        SingleObserver<Integer> NbpObserver = TestHelper.mockSingleObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstOrDefaultWithEmpty() {
        Single<Integer> o = Observable.<Integer> empty()
                .first(1);

        SingleObserver<Integer> NbpObserver = TestHelper.mockSingleObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstOrDefaultWithPredicate() {
        Single<Integer> o = Observable.just(1, 2, 3, 4, 5, 6)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer t1) {
                        return t1 % 2 == 0;
                    }
                })
                .first(8);

        SingleObserver<Integer> NbpObserver = TestHelper.mockSingleObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstOrDefaultWithPredicateAndOneElement() {
        Single<Integer> o = Observable.just(1, 2)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer t1) {
                        return t1 % 2 == 0;
                    }
                })
                .first(4);

        SingleObserver<Integer> NbpObserver = TestHelper.mockSingleObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFirstOrDefaultWithPredicateAndEmpty() {
        Single<Integer> o = Observable.just(1)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer t1) {
                        return t1 % 2 == 0;
                    }
                })
                .first(2);
        
        SingleObserver<Integer> NbpObserver = TestHelper.mockSingleObserver();
        o.subscribe(NbpObserver);

        InOrder inOrder = inOrder(NbpObserver);
        inOrder.verify(NbpObserver, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }
}