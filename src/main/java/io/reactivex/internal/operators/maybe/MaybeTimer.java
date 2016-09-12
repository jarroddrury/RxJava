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

package io.reactivex.internal.operators.maybe;

import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.Scheduler;
import io.reactivex.internal.disposables.SequentialDisposable;

/**
 * Signals a 0L after the specified delay
 */
public final class MaybeTimer extends Maybe<Long> {

    final long delay;

    final TimeUnit unit;

    final Scheduler scheduler;

    public MaybeTimer(long delay, TimeUnit unit, Scheduler scheduler) {
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    @Override
    protected void subscribeActual(final MaybeObserver<? super Long> observer) {
        SequentialDisposable sd = new SequentialDisposable();

        observer.onSubscribe(sd);

        sd.replace(scheduler.scheduleDirect(new Runnable() {
            @Override
            public void run() {
                observer.onSuccess(0L);
            }
        }, delay, unit));
    }
}
