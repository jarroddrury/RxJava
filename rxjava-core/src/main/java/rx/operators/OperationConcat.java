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

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.SerialSubscription;
import rx.util.functions.Func1;

/**
 * Returns an Observable that emits the items emitted by two or more Observables, one after the
 * other.
 * <p>
 * <img width="640" src="https://github.com/Netflix/RxJava/wiki/images/rx-operators/concat.png">
 */
public final class OperationConcat {

    /**
     * Combine the observable sequences from the list of Observables into one
     * observable sequence without any transformation. If either the outer
     * observable or an inner observable calls onError, we will call onError.
     * <p/>
     * 
     * @param sequences
     *            An observable sequence of elements to project.
     * @return An observable sequence whose elements are the result of combining the output from the list of Observables.
     */
    public static <T> OnSubscribeFunc<T> concat(final Observable<? extends T>... sequences) {
        return concat(Observable.from(sequences));
    }

    public static <T> OnSubscribeFunc<T> concat(final Iterable<? extends Observable<? extends T>> sequences) {
        return concat(Observable.from(sequences));
    }

    public static <T> OnSubscribeFunc<T> concat(final Observable<? extends Observable<? extends T>> sequences) {
        return new OnSubscribeFunc<T>() {
            @Override
            public Subscription onSubscribe(Observer<? super T> t1) {
                return new Concat<T>(sequences).onSubscribe(t1);
            }            
        };
    }

    private static class Concat<T> implements OnSubscribeFunc<T> {
        private Observable<? extends Observable<? extends T>> sequences;
        private SafeObservableSubscription innerSubscription = null;

        public Concat(Observable<? extends Observable<? extends T>> sequences) {
            this.sequences = sequences;
        }

        public Subscription onSubscribe(final Observer<? super T> observer) {
            final AtomicBoolean completedOrErred = new AtomicBoolean(false);
            final AtomicBoolean allSequencesReceived = new AtomicBoolean(false);
            final Queue<Observable<? extends T>> nextSequences = new ConcurrentLinkedQueue<Observable<? extends T>>();
            final SafeObservableSubscription outerSubscription = new SafeObservableSubscription();

            final Observer<T> reusableObserver = new Observer<T>() {
                @Override
                public void onNext(T item) {
                    observer.onNext(item);
                }

                @Override
                public void onError(Throwable e) {
                    if (completedOrErred.compareAndSet(false, true)) {
                        outerSubscription.unsubscribe();
                        observer.onError(e);
                    }
                }

                @Override
                public void onCompleted() {
                    synchronized (nextSequences) {
                        if (nextSequences.isEmpty()) {
                            // No new sequences available at the moment
                            innerSubscription = null;
                            if (allSequencesReceived.get()) {
                                // No new sequences are coming, we are finished
                                if (completedOrErred.compareAndSet(false, true)) {
                                    observer.onCompleted();
                                }
                            }
                        } else {
                            // Continue on to the next sequence
                            innerSubscription = new SafeObservableSubscription();
                            innerSubscription.wrap(nextSequences.poll().subscribe(this));
                        }
                    }
                }
            };

            outerSubscription.wrap(sequences.subscribe(new Observer<Observable<? extends T>>() {
                @Override
                public void onNext(Observable<? extends T> nextSequence) {
                    synchronized (nextSequences) {
                        if (innerSubscription == null) {
                            // We are currently not subscribed to any sequence
                            innerSubscription = new SafeObservableSubscription();
                            innerSubscription.wrap(nextSequence.subscribe(reusableObserver));
                        } else {
                            // Put this sequence at the end of the queue
                            nextSequences.add(nextSequence);
                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    if (completedOrErred.compareAndSet(false, true)) {
                        Subscription q;
                        synchronized (nextSequences) {
                            q = innerSubscription;
                        }
                        if (q != null) {
                            q.unsubscribe();
                        }
                        observer.onError(e);
                    }
                }

                @Override
                public void onCompleted() {
                    allSequencesReceived.set(true);
                    Subscription q;
                    synchronized (nextSequences) {
                        q = innerSubscription;
                    }
                    if (q == null) {
                        // We are not subscribed to any sequence, and none are coming anymore
                        if (completedOrErred.compareAndSet(false, true)) {
                            observer.onCompleted();
                        }
                    }
                }
            }));

            return new Subscription() {
                @Override
                public void unsubscribe() {
                    Subscription q;
                    synchronized (nextSequences) {
                        q = innerSubscription;
                    }
                    if (q != null) {
                        q.unsubscribe();
                    }
                    outerSubscription.unsubscribe();
                }
            };
        }
    }
    
    /**
     * Concatenates the observable sequences obtained by running the
     * resultSelector for each element in the given Iterable source.
     * @param <T> the source value type
     * @param <R> the result value type
     * @param source the source sequence
     * @param resultSelector the result selector function to return
     *                       an Observable sequence for concatenation
     * @return a subscriber function
     */
    public static <T, R> OnSubscribeFunc<R> forIterable(Iterable<? extends T> source, Func1<? super T, ? extends Observable<? extends R>> resultSelector) {
        return new For<T, R>(source, resultSelector);
    }
    
    /**
     * For each source element in an iterable, concatenate
     * the observables returned by a selector function.
     * @param <T> the source value type
     * @param <R> the result value type
     */
    private static final class For<T, R> implements OnSubscribeFunc<R> {
        final Iterable<? extends T> source;
        final Func1<? super T, ? extends Observable<? extends R>> resultSelector;
        
        public For(Iterable<? extends T> source, Func1<? super T, ? extends Observable<? extends R>> resultSelector) {
            this.source = source;
            this.resultSelector = resultSelector;
        }
        
        @Override
        public Subscription onSubscribe(Observer<? super R> t1) {
            SerialSubscription ssub = new SerialSubscription();
            Iterator<? extends T> it = source.iterator();
            ValueObserver<T, R> vo = new ValueObserver<T, R>(t1, ssub, it, resultSelector);
            vo.onCompleted(); // trigger the first subscription
            return ssub;
        }
    }
    /**
     * The observer of values in the returned observables.
     */
    private static final class ValueObserver<T, R> implements Observer<R> {
        final Observer<? super R> observer;
        final SerialSubscription cancel;
        final Iterator<? extends T> iterator;
        final Func1<? super T, ? extends Observable<? extends R>> resultSelector;
        public ValueObserver(
            Observer<? super R> observer,
            SerialSubscription cancel,
            Iterator<? extends T> iterator,
            Func1<? super T, ? extends Observable<? extends R>> resultSelector
        ) {
            this.observer = observer;
            this.cancel = cancel;
            this.iterator = iterator;
            this.resultSelector = resultSelector;
        }
        
        @Override
        public void onNext(R args) {
            observer.onNext(args);
        }
        
        @Override
        public void onError(Throwable e) {
            observer.onError(e);
            cancel.unsubscribe();
        }
        
        @Override
        public void onCompleted() {
            try {
                if (iterator.hasNext()) {
                    T v = iterator.next();
                    Observable<? extends R> o = resultSelector.call(v);
                    cancel.setSubscription(o.subscribe(this));
                    return;
                }
            } catch (Throwable t) {
                observer.onError(t);
                cancel.unsubscribe();
                return;
            }
            observer.onCompleted();
            cancel.unsubscribe();
        }
        
    }
}
