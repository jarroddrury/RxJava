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
package rx.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rx.*;
import rx.Observable.OnSubscribe;
import rx.Observable;
import rx.exceptions.TestException;
import rx.functions.*;
import rx.observers.TestSubscriber;
import rx.schedulers.*;
import rx.subjects.PublishSubject;

public class CachedObservableTest {
    @Test
    public void testColdReplayNoBackpressure() {
        CachedObservable<Integer> source = CachedObservable.from(Observable.range(0, 1000));
        
        assertFalse("Source is connected!", source.isConnected());
        assertFalse("Source is unsubscribed!", source.isUnsubscribed());
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        source.subscribe(ts);

        assertTrue("Source is not connected!", source.isConnected());
        assertTrue("Source is not unsubscribed!", source.isUnsubscribed());
        assertFalse("Subscribers retained!", source.hasObservers());
        
        ts.assertNoErrors();
        ts.assertTerminalEvent();
        List<Integer> onNextEvents = ts.getOnNextEvents();
        assertEquals(1000, onNextEvents.size());

        for (int i = 0; i < 1000; i++) {
            assertEquals((Integer)i, onNextEvents.get(i));
        }
    }
    @Test
    public void testColdReplayBackpressure() {
        CachedObservable<Integer> source = Observable.range(0, 1000).toCached();
        
        assertFalse("Source is connected!", source.isConnected());
        assertFalse("Source is unsubscribed!", source.isUnsubscribed());
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        ts.requestMore(10);
        
        source.subscribe(ts);

        assertTrue("Source is not connected!", source.isConnected());
        assertTrue("Source is not unsubscribed!", source.isUnsubscribed());
        assertTrue("Subscribers not retained!", source.hasObservers());
        
        ts.assertNoErrors();
        assertTrue(ts.getOnCompletedEvents().isEmpty());
        List<Integer> onNextEvents = ts.getOnNextEvents();
        assertEquals(10, onNextEvents.size());

        for (int i = 0; i < 10; i++) {
            assertEquals((Integer)i, onNextEvents.get(i));
        }
        
        ts.unsubscribe();
        assertFalse("Subscribers retained!", source.hasObservers());
    }
    
    @Test
    public void testTerminalReplayedWithoutRequest() {
        CachedObservable<Integer> source = Observable.range(1, 1000).toCached();
        source.complete();

        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        ts.requestMore(0);
        
        source.subscribe(ts);
        
        ts.assertTerminalEvent();
        
        assertEquals(1, source.cachedEventCount());
    }
    
    @Test
    public void testRegularTerminalNotOverwritten() {
        CachedObservable<Integer> source = Observable.<Integer>error(new TestException()).toCached();
        // trigger the first connection
        source.subscribe(new TestSubscriber<Integer>());
        
        source.complete();


        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        ts.requestMore(0);
        
        source.subscribe(ts);

        ts.assertTerminalEvent();
        assertEquals(1, ts.getOnErrorEvents().size());
        assertTrue(ts.getOnCompletedEvents().isEmpty());
        
        assertEquals(1, source.cachedEventCount());
    }
    
    @Test
    public void testForcedComplete() {
        CachedObservable<Integer> source = Observable.<Integer>never().toCached();
        source.complete();
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        source.subscribe(ts);
        
        ts.assertTerminalEvent();
        ts.assertNoErrors();
    }
    @Test
    public void testForcedError() {
        CachedObservable<Integer> source = Observable.<Integer>never().toCached();
        source.completeExceptionally(new TestException());
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        source.subscribe(ts);
        ts.assertTerminalEvent();
        
        assertEquals(1, ts.getOnErrorEvents().size());
        assertTrue(ts.getOnErrorEvents().get(0) instanceof TestException);
    }
    
    @Test
    public void testForcedCompetionWhileActive() {
        PublishSubject<Integer> source = PublishSubject.create();
        CachedObservable<Integer> cached = source.toCached();

        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        cached.subscribe(ts);
        
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        
        cached.complete();
        
        source.onNext(5);
        source.onNext(6);
        source.onNext(7);
        source.onCompleted();
        
        ts.assertTerminalEvent();
        ts.assertNoErrors();
        ts.assertReceivedOnNext(Arrays.asList(1, 2, 3, 4));
    }
    
    @Test
    public void testCache() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        Observable<String> o = Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(final Subscriber<? super String> observer) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        counter.incrementAndGet();
                        System.out.println("published observable being executed");
                        observer.onNext("one");
                        observer.onCompleted();
                    }
                }).start();
            }
        }).toCached();

        // we then expect the following 2 subscriptions to get that same value
        final CountDownLatch latch = new CountDownLatch(2);

        // subscribe once
        o.subscribe(new Action1<String>() {

            @Override
            public void call(String v) {
                assertEquals("one", v);
                System.out.println("v: " + v);
                latch.countDown();
            }
        });

        // subscribe again
        o.subscribe(new Action1<String>() {

            @Override
            public void call(String v) {
                assertEquals("one", v);
                System.out.println("v: " + v);
                latch.countDown();
            }
        });

        if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
            fail("subscriptions did not receive values");
        }
        assertEquals(1, counter.get());
    }

    @Test
    public void testUnsubscribeSource() {
        Action0 unsubscribe = mock(Action0.class);
        Observable<Integer> o = Observable.just(1).doOnUnsubscribe(unsubscribe).toCached();
        o.subscribe();
        o.subscribe();
        o.subscribe();
        verify(unsubscribe, times(1)).call();
    }
    
    @Test
    public void testTake() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();

        CachedObservable<Integer> cached = Observable.range(1, 100).toCached();
        cached.take(10).subscribe(ts);
        
        ts.assertNoErrors();
        ts.assertTerminalEvent();
        ts.assertReceivedOnNext(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        ts.assertUnsubscribed();
        assertFalse(cached.hasObservers());
    }
    
    @Test
    public void testAsync() {
        Observable<Integer> source = Observable.range(1, 10000);
        for (int i = 0; i < 100; i++) {
            TestSubscriber<Integer> ts1 = new TestSubscriber<Integer>();
            
            CachedObservable<Integer> cached = source.toCached();
            
            cached.observeOn(Schedulers.computation()).subscribe(ts1);
            
            ts1.awaitTerminalEvent(2, TimeUnit.SECONDS);
            ts1.assertNoErrors();
            ts1.assertTerminalEvent();
            assertEquals(10000, ts1.getOnNextEvents().size());
            
            TestSubscriber<Integer> ts2 = new TestSubscriber<Integer>();
            cached.observeOn(Schedulers.computation()).subscribe(ts2);
            
            ts2.awaitTerminalEvent(2, TimeUnit.SECONDS);
            ts2.assertNoErrors();
            ts2.assertTerminalEvent();
            assertEquals(10000, ts2.getOnNextEvents().size());
        }
    }
    @Test
    public void testAsyncComeAndGo() {
        Observable<Long> source = Observable.timer(1, 1, TimeUnit.MILLISECONDS)
                .take(1000)
                .subscribeOn(Schedulers.io());
        CachedObservable<Long> cached = source.toCached();
        
        Observable<Long> output = cached.observeOn(Schedulers.computation());
        
        List<TestSubscriber<Long>> list = new ArrayList<TestSubscriber<Long>>(100);
        for (int i = 0; i < 100; i++) {
            TestSubscriber<Long> ts = new TestSubscriber<Long>();
            list.add(ts);
            output.skip(i * 10).take(10).subscribe(ts);
        }

        List<Long> expected = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            expected.add((long)(i - 10));
        }
        int j = 0;
        for (TestSubscriber<Long> ts : list) {
            ts.awaitTerminalEvent(3, TimeUnit.SECONDS);
            ts.assertNoErrors();
            ts.assertTerminalEvent();
            
            for (int i = j * 10; i < j * 10 + 10; i++) {
                expected.set(i - j * 10, (long)i);
            }
            
            ts.assertReceivedOnNext(expected);
            
            j++;
        }
    }
    
    @Test
    public void testNoMissingBackpressureException() {
        final int m = 4 * 1000 * 1000;
        Observable<Integer> firehose = Observable.create(new OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> t) {
                for (int i = 0; i < m; i++) {
                    t.onNext(i);
                }
                t.onCompleted();
            }
        });
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        firehose.toCached().observeOn(Schedulers.computation()).takeLast(100).subscribe(ts);
        
        ts.awaitTerminalEvent(3, TimeUnit.SECONDS);
        ts.assertNoErrors();
        ts.assertTerminalEvent();
        
        assertEquals(100, ts.getOnNextEvents().size());
    }
}
