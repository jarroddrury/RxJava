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
package io.reactivex.internal.subscribers.flowable;

import java.util.concurrent.CountDownLatch;

import org.reactivestreams.*;

import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.Exceptions;

public abstract class BlockingSingleSubscriber<T> extends CountDownLatch
implements Subscriber<T>, Disposable {

    T value;
    Throwable error;
    
    Subscription s;
    
    volatile boolean cancelled;

    public BlockingSingleSubscriber() {
        super(1);
    }

    @Override
    public final void onSubscribe(Subscription s) {
        this.s = s;
        if (!cancelled) {
            s.request(Long.MAX_VALUE);
            if (cancelled) {
                s.cancel();
            }
        }
    }
    
    @Override
    public final void onComplete() {
        countDown();
    }
    
    @Override
    public final void dispose() {
        cancelled = true;
        Subscription s = this.s;
        if (s != null) {
            s.cancel();
        }
    }
    
    @Override
    public final boolean isDisposed() {
        return cancelled;
    }
    
    /**
     * Block until the first value arrives and return it, otherwise
     * return null for an empty source and rethrow any exception.
     * @return the first value or null if the source is empty
     */
    public final T blockingGet() {
        if (getCount() != 0) {
            try {
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw Exceptions.propagate(ex);
            }
        }
        
        Throwable e = error;
        if (e != null) {
            Exceptions.propagate(e);
        }
        return value;
    }
}
