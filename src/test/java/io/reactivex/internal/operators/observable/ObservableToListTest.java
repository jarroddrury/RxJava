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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.mockito.Mockito;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.TestHelper;

public class ObservableToListTest {

    @Test
    public void testList() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> NbpObservable = w.toList();

        SingleObserver<List<String>> NbpObserver = TestHelper.mockSingleObserver();
        NbpObservable.subscribe(NbpObserver);
        verify(NbpObserver, times(1)).onSuccess(Arrays.asList("one", "two", "three"));
        verify(NbpObserver, Mockito.never()).onError(any(Throwable.class));
    }
    
    @Test
    public void testListViaObservable() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> NbpObservable = w.toList();

        SingleObserver<List<String>> NbpObserver = TestHelper.mockSingleObserver();
        NbpObservable.subscribe(NbpObserver);
        verify(NbpObserver, times(1)).onSuccess(Arrays.asList("one", "two", "three"));
        verify(NbpObserver, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void testListMultipleSubscribers() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> NbpObservable = w.toList();

        SingleObserver<List<String>> o1 = TestHelper.mockSingleObserver();
        NbpObservable.subscribe(o1);

        SingleObserver<List<String>> o2 = TestHelper.mockSingleObserver();
        NbpObservable.subscribe(o2);

        List<String> expected = Arrays.asList("one", "two", "three");

        verify(o1, times(1)).onSuccess(expected);
        verify(o1, Mockito.never()).onError(any(Throwable.class));

        verify(o2, times(1)).onSuccess(expected);
        verify(o2, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    @Ignore("Null values are not allowed")
    public void testListWithNullValue() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", null, "three"));
        Single<List<String>> NbpObservable = w.toList();

        SingleObserver<Object> NbpObserver = TestHelper.mockSingleObserver();
        NbpObservable.subscribe(NbpObserver);
        verify(NbpObserver, times(1)).onSuccess(Arrays.asList("one", null, "three"));
        verify(NbpObserver, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void testListWithBlockingFirst() {
        Observable<String> o = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        List<String> actual = o.toList().blockingGet();
        Assert.assertEquals(Arrays.asList("one", "two", "three"), actual);
    }

    static void await(CyclicBarrier cb) {
        try {
            cb.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (BrokenBarrierException ex) {
            ex.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void capacityHint() {
        Observable.range(1, 10)
        .toList(4)
        .test()
        .assertResult(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }
}