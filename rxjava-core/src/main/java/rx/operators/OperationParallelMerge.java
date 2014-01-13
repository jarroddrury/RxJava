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

import java.util.concurrent.atomic.AtomicLong;

import rx.IObservable;
import rx.Observable;
import rx.Scheduler;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;
import rx.util.functions.Func1;

public class OperationParallelMerge {

    public static <T> Observable<Observable<T>> parallelMerge(final IObservable<? extends IObservable<T>> source, final int parallelObservables) {
        return parallelMerge(source, parallelObservables, Schedulers.currentThread());
    }

    public static <T> Observable<Observable<T>> parallelMerge(final IObservable<? extends IObservable<T>> isource, final int parallelObservables, final Scheduler scheduler) {
        return parallelMergeHelper(isource, parallelObservables, scheduler);
    }

    private static <T, OB extends IObservable<T>> Observable<Observable<T>> parallelMergeHelper(final IObservable<OB> isource, final int parallelObservables, final Scheduler scheduler) {
        final Observable<OB> source = Observable.from(isource);
        return source.groupBy(new Func1<IObservable<T>, Integer>() {
            final AtomicLong rollingCount = new AtomicLong();

            @Override
            public Integer call(IObservable<T> o) {
                return (int) rollingCount.incrementAndGet() % parallelObservables;
            }
        }).map(new Func1<GroupedObservable<Integer, OB>, Observable<T>>() {

            @Override
            public Observable<T> call(GroupedObservable<Integer, OB> o) {
                return Observable.merge(o).observeOn(scheduler);
            }

        });

    }

}
