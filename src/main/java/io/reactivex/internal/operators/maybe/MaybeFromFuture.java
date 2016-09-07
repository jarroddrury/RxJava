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

import java.util.concurrent.*;

import io.reactivex.*;
import io.reactivex.disposables.*;

/**
 * Waits until the source Future completes or the wait times out; treates null
 * result as indication for empty.
 *
 * @param <T> the value type
 */
public final class MaybeFromFuture<T> extends Maybe<T> {

    final Future<? extends T> future;
    
    final long timeout;
    
    final TimeUnit unit;
    
    public MaybeFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {
        this.future = future;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        Disposable d = Disposables.empty();
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        
        T v;
        try {
            if (timeout <= 0L) {
                v = future.get();
            } else {
                v = future.get(timeout, unit);
            }
        } catch (InterruptedException ex) {
            if (d.isDisposed()) {
                return;
            }
            observer.onError(ex);
            return;
        } catch (ExecutionException ex) {
            if (d.isDisposed()) {
                return;
            }
            observer.onError(ex.getCause());
            return;
        } catch (TimeoutException ex) {
            if (d.isDisposed()) {
                return;
            }
            observer.onError(ex);
            return;
        }
        if (d.isDisposed()) {
            return;
        }
        if (v == null) {
            observer.onComplete();
        } else {
            observer.onSuccess(v);
        }
    }
}
