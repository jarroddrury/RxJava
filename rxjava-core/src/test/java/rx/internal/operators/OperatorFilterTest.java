/**
 * Copyright 2014 Netflix, Inc.
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
package rx.internal.operators;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.mockito.Mockito;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;

public class OperatorFilterTest {

    @Test
    public void testFilter() {
        Observable<String> w = Observable.from("one", "two", "three");
        Observable<String> observable = w.filter(new Func1<String, Boolean>() {

            @Override
            public Boolean call(String t1) {
                return t1.equals("two");
            }
        });

        @SuppressWarnings("unchecked")
        Observer<String> observer = mock(Observer.class);
        observable.subscribe(observer);
        verify(observer, Mockito.never()).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, Mockito.never()).onNext("three");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onCompleted();
    }

    /**
     * Make sure we are adjusting subscriber.request() for filtered items
     */
    @Test(timeout = 500)
    public void testWithBackpressure() throws InterruptedException {
        Observable<String> w = Observable.from("one", "two", "three");
        Observable<String> o = w.filter(new Func1<String, Boolean>() {

            @Override
            public Boolean call(String t1) {
                return t1.equals("three");
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        Subscriber<String> s = new Subscriber<String>() {

            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onNext(String t) {
                System.out.println("Received: " + t);
                // request more each time we receive
                request(1);
            }

        };
        // this means it will only request "one" and "two", expecting to receive them before requesting more
        s.request(2);

        o.subscribe(s);

        // this will wait forever unless OperatorTake handles the request(n) on filtered items
        latch.await();
    }
}
