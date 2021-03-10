/*
 * Copyright (c) 2016-present, RxJava Contributors.
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

package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava3.annotations.NonNull;
import org.junit.*;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableWindowWithStartEndObservableTest extends RxJavaTest {

    private TestScheduler scheduler;
    private Scheduler.Worker innerScheduler;

    @Before
    public void before() {
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
    }

    @Test
    public void observableBasedOpenerAndCloser() {
        final List<String> list = new ArrayList<>();
        final List<List<String>> lists = new ArrayList<>();

        Observable<String> source = Observable.unsafeCreate(innerObserver -> {
            innerObserver.onSubscribe(Disposable.empty());
            push(innerObserver, "one", 10);
            push(innerObserver, "two", 60);
            push(innerObserver, "three", 110);
            push(innerObserver, "four", 160);
            push(innerObserver, "five", 210);
            complete(innerObserver, 500);
        });

        Observable<Object> openings = Observable.unsafeCreate(innerObserver -> {
            innerObserver.onSubscribe(Disposable.empty());
            push(innerObserver, new Object(), 50);
            push(innerObserver, new Object(), 200);
            complete(innerObserver, 250);
        });

        Function<Object, Observable<Object>> closer = opening -> Observable.unsafeCreate(innerObserver -> {
            innerObserver.onSubscribe(Disposable.empty());
            push(innerObserver, new Object(), 100);
            complete(innerObserver, 101);
        });

        Observable<Observable<String>> windowed = source.window(openings, closer);
        windowed.subscribe(observeWindow(list, lists));

        scheduler.advanceTimeTo(500, TimeUnit.MILLISECONDS);
        assertEquals(2, lists.size());
        assertEquals(lists.get(0), list("two", "three"));
        assertEquals(lists.get(1), list("five"));
    }

    private List<String> list(String... args) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, args);
        return list;
    }

    private <T> void push(final Observer<T> observer, final T value, int delay) {
        innerScheduler.schedule(() -> observer.onNext(value), delay, TimeUnit.MILLISECONDS);
    }

    private void complete(final Observer<?> observer, int delay) {
        innerScheduler.schedule(observer::onComplete, delay, TimeUnit.MILLISECONDS);
    }

    private Consumer<Observable<String>> observeWindow(final List<String> list, final List<List<String>> lists) {
        return stringObservable -> stringObservable.subscribe(new DefaultObserver<String>() {
            @Override
            public void onComplete() {
                lists.add(new ArrayList<>(list));
                list.clear();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                fail(e.getMessage());
            }

            @Override
            public void onNext(@NonNull String args) {
                list.add(args);
            }
        });
    }

    @Test
    public void noUnsubscribeAndNoLeak() {
        PublishSubject<Integer> source = PublishSubject.create();

        PublishSubject<Integer> open = PublishSubject.create();
        final PublishSubject<Integer> close = PublishSubject.create();

        TestObserver<Observable<Integer>> to = new TestObserver<>();

        source.window(open, (Function<Integer, Observable<Integer>>) t -> close)
        .doOnNext(w -> {
            w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer()); // avoid abandonment
        })
        .subscribe(to);

        open.onNext(1);
        source.onNext(1);

        assertTrue(open.hasObservers());
        assertTrue(close.hasObservers());

        close.onNext(1);

        assertFalse(close.hasObservers());

        source.onComplete();

        to.assertComplete();
        to.assertNoErrors();
        to.assertValueCount(1);

        // 2.0.2 - not anymore
//        assertTrue("Not cancelled!", ts.isCancelled());
        assertFalse(open.hasObservers());
        assertFalse(close.hasObservers());
    }

    @Test
    public void unsubscribeAll() {
        PublishSubject<Integer> source = PublishSubject.create();

        PublishSubject<Integer> open = PublishSubject.create();
        final PublishSubject<Integer> close = PublishSubject.create();

        TestObserver<Observable<Integer>> to = new TestObserver<>();

        source.window(open, (Function<Integer, Observable<Integer>>) t -> close)
        .doOnNext(w -> {
            w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer()); // avoid abandonment
        })
        .subscribe(to);

        open.onNext(1);

        assertTrue(open.hasObservers());
        assertTrue(close.hasObservers());

        to.dispose();

        // Disposing the outer sequence stops the opening of new windows
        assertFalse(open.hasObservers());
        // FIXME subject has subscribers because of the open window
        assertTrue(close.hasObservers());
    }

    @Test
    public void boundarySelectorNormal() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> start = PublishSubject.create();
        final PublishSubject<Integer> end = PublishSubject.create();

        TestObserver<Integer> to = source.window(start, v -> end)
        .flatMap(Functions.identity())
        .test();

        start.onNext(0);

        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);

        start.onNext(1);

        source.onNext(5);
        source.onNext(6);

        end.onNext(1);

        start.onNext(2);

        TestHelper.emit(source, 7, 8);

        to.assertResult(1, 2, 3, 4, 5, 5, 6, 6, 7, 8);
    }

    @Test
    public void startError() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> start = PublishSubject.create();
        final PublishSubject<Integer> end = PublishSubject.create();

        TestObserver<Integer> to = source.window(start, v -> end)
        .flatMap(Functions.identity())
        .test();

        start.onError(new TestException());

        to.assertFailure(TestException.class);

        assertFalse("Source has observers!", source.hasObservers());
        assertFalse("Start has observers!", start.hasObservers());
        assertFalse("End has observers!", end.hasObservers());
    }

    @Test
    @SuppressUndeliverable
    public void endError() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> start = PublishSubject.create();
        final PublishSubject<Integer> end = PublishSubject.create();

        TestObserver<Integer> to = source.window(start, v -> end)
        .flatMap(Functions.identity())
        .test();

        start.onNext(1);
        end.onError(new TestException());

        to.assertFailure(TestException.class);

        assertFalse("Source has observers!", source.hasObservers());
        assertFalse("Start has observers!", start.hasObservers());
        assertFalse("End has observers!", end.hasObservers());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).window(Observable.just(2), Functions.justFunction(Observable.never())));
    }

    @Test
    public void reentrant() {
        final Subject<Integer> ps = PublishSubject.create();

        TestObserver<Integer> to = new TestObserver<Integer>() {
            @Override
            public void onNext(@NonNull Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                    ps.onComplete();
                }
            }
        };

        ps.window(BehaviorSubject.createDefault(1), Functions.justFunction(Observable.never()))
        .flatMap((Function<Observable<Integer>, ObservableSource<Integer>>) v -> v)
        .subscribe(to);

        ps.onNext(1);

        to
        .awaitDone(1, TimeUnit.SECONDS)
        .assertResult(1, 2);
    }

    @Test
    public void badSourceCallable() {
        TestHelper.checkBadSourceObservable((Function<Observable<Object>, Object>) o -> o.window(Observable.just(1), Functions.justFunction(Observable.never())), false, 1, 1, (Object[])null);
    }

    @Test
    public void windowCloseIngoresCancel() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            BehaviorSubject.createDefault(1)
            .window(BehaviorSubject.createDefault(1), (Function<Integer, Observable<Integer>>) f -> new Observable<Integer>() {
                @Override
                protected void subscribeActual(
                        @NonNull Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new TestException());
                }
            })
            .doOnNext(w -> {
                w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer()); // avoid abandonment
            })
            .test()
            .assertValueCount(1)
            .assertNoErrors()
            .assertNotComplete();

            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    static Observable<Integer> observableDisposed(final AtomicBoolean ref) {
        return Observable.just(1).concatWith(Observable.never())
                .doOnDispose(() -> ref.set(true));
    }

    @Test
    public void mainAndBoundaryDisposeOnNoWindows() {
        AtomicBoolean mainDisposed = new AtomicBoolean();
        AtomicBoolean openDisposed = new AtomicBoolean();
        final AtomicBoolean closeDisposed = new AtomicBoolean();

        observableDisposed(mainDisposed)
        .window(observableDisposed(openDisposed), v -> observableDisposed(closeDisposed))
        .doOnNext(w -> {
            w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer()); // avoid abandonment
        })
        .to(TestHelper.testConsumer())
        .assertSubscribed()
        .assertNoErrors()
        .assertNotComplete()
        .dispose();

        assertTrue(mainDisposed.get());
        assertTrue(openDisposed.get());
        assertTrue(closeDisposed.get());
    }

    @Test
    public void cancellingWindowCancelsUpstream() {
        PublishSubject<Integer> ps = PublishSubject.create();

        TestObserver<Integer> to = ps.window(Observable.just(1).concatWith(Observable.never()), Functions.justFunction(Observable.never()))
        .take(1)
        .flatMap((Function<Observable<Integer>, Observable<Integer>>) w -> w.take(1))
        .test();

        assertTrue(ps.hasObservers());

        ps.onNext(1);

        to
        .assertResult(1);

        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void windowAbandonmentCancelsUpstream() {
        PublishSubject<Integer> ps = PublishSubject.create();

        final AtomicReference<Observable<Integer>> inner = new AtomicReference<>();

        TestObserver<Observable<Integer>> to = ps.window(Observable.just(1).concatWith(Observable.never()),
                Functions.justFunction(Observable.never()))
        .doOnNext(inner::set)
        .test();

        assertTrue(ps.hasObservers());

        to
        .assertValueCount(1)
        ;

        ps.onNext(1);

        assertTrue(ps.hasObservers());

        to.dispose();

        to
        .assertValueCount(1)
        .assertNoErrors()
        .assertNotComplete();

        assertFalse("Subject still has observers!", ps.hasObservers());

        inner.get().test().assertResult();
    }

    @Test
    public void closingIndicatorFunctionCrash() {

        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> boundary = PublishSubject.create();

        TestObserver<Observable<Integer>> to = source.window(boundary, (Function<Integer, Observable<Object>>) end -> {
            throw new TestException();
        })
        .test()
        ;

        to.assertEmpty();

        boundary.onNext(1);

        to.assertFailure(TestException.class);

        assertFalse(source.hasObservers());
        assertFalse(boundary.hasObservers());
    }

    @Test
    public void mainError() {
        Observable.error(new TestException())
        .window(Observable.never(), Functions.justFunction(Observable.never()))
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.window(Observable.never(), v -> Observable.never()));
    }

    @Test
    public void openError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            TestException ex1 = new TestException();
            TestException ex2 = new TestException();
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();
                AtomicReference<Observer<? super Integer>> ref2 = new AtomicReference<>();

                Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);
                Observable<Integer> o2 = Observable.<Integer>unsafeCreate(ref2::set);

                TestObserver<Observable<Integer>> to = BehaviorSubject.createDefault(1)
                .window(o1, v -> o2)
                .doOnNext(Observable::test)
                .test();

                ref1.get().onSubscribe(Disposable.empty());
                ref1.get().onNext(1);
                ref2.get().onSubscribe(Disposable.empty());

                TestHelper.race(
                        () -> ref1.get().onError(ex1),
                        () -> ref2.get().onError(ex2)
                );

                to.assertError(RuntimeException.class);

                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }

                errors.clear();
            }
        });
    }

    @Test
    public void closeError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();
            AtomicReference<Observer<? super Integer>> ref2 = new AtomicReference<>();

            Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);
            Observable<Integer> o2 = Observable.<Integer>unsafeCreate(ref2::set);

            TestObserver<Integer> to = BehaviorSubject.createDefault(1)
            .window(o1, v -> o2)
            .flatMap(v -> v)
            .test();

            ref1.get().onSubscribe(Disposable.empty());
            ref1.get().onNext(1);
            ref2.get().onSubscribe(Disposable.empty());

            ref2.get().onError(new TestException());
            ref2.get().onError(new TestException());

            to.assertFailure(TestException.class);

            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void upstreamFailsBeforeFirstWindow() {
        Observable.error(new TestException())
        .window(Observable.never(), v -> Observable.never())
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void windowOpenMainCompletes() {
        AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();

        PublishSubject<Object> ps = PublishSubject.create();
        Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);

        AtomicInteger counter = new AtomicInteger();

        TestObserver<Observable<Object>> to = ps
        .window(o1, v -> Observable.never())
        .doOnNext(w -> {
            if (counter.getAndIncrement() == 0) {
                ref1.get().onNext(2);
                ps.onNext(1);
                ps.onComplete();
            }
            w.test();
        })
        .test();

        ref1.get().onSubscribe(Disposable.empty());
        ref1.get().onNext(1);

        to.assertComplete();
    }

    @Test
    public void windowOpenMainError() {
        AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();

        PublishSubject<Object> ps = PublishSubject.create();
        Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);

        AtomicInteger counter = new AtomicInteger();

        TestObserver<Observable<Object>> to = ps
        .window(o1, v -> Observable.never())
        .doOnNext(w -> {
            if (counter.getAndIncrement() == 0) {
                ref1.get().onNext(2);
                ps.onNext(1);
                ps.onError(new TestException());
            }
            w.test();
        })
        .test();

        ref1.get().onSubscribe(Disposable.empty());
        ref1.get().onNext(1);

        to.assertError(TestException.class);
    }

    @Test
    public void windowOpenIgnoresDispose() {
        AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();

        PublishSubject<Object> ps = PublishSubject.create();
        Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);

        TestObserver<Observable<Object>> to = ps
        .window(o1, v -> Observable.never())
        .take(1)
        .doOnNext(Observable::test)
        .test();

        ref1.get().onSubscribe(Disposable.empty());
        ref1.get().onNext(1);
        ref1.get().onNext(2);

        to.assertValueCount(1);
    }

    @Test
    public void mainIgnoresCancelBeforeOnError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Observable.unsafeCreate(s -> {
                s.onSubscribe(Disposable.empty());
                s.onNext(1);
                s.onError(new IOException());
            })
            .window(BehaviorSubject.createDefault(1), v -> Observable.error(new TestException()))
            .doOnNext(Observable::test)
            .test()
            .assertError(TestException.class);

            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        });
    }
}
