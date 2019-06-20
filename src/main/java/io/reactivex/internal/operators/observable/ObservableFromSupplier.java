/**
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

package io.reactivex.internal.operators.observable;

import io.reactivex.*;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Supplier;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.observers.DeferredScalarDisposable;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Calls a Supplier and emits its resulting single value or signals its exception.
 * @param <T> the value type
 * @since 3.0.0
 */
public final class ObservableFromSupplier<T> extends Observable<T> implements Supplier<T> {

    final Supplier<? extends T> supplier;

    public ObservableFromSupplier(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void subscribeActual(Observer<? super T> observer) {
        DeferredScalarDisposable<T> d = new DeferredScalarDisposable<T>(observer);
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        T value;
        try {
            value = ObjectHelper.requireNonNull(supplier.get(), "Supplier returned null");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            if (!d.isDisposed()) {
                observer.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
            return;
        }
        d.complete(value);
    }

    @Override
    public T get() throws Throwable {
        return ObjectHelper.requireNonNull(supplier.get(), "The supplier returned a null value");
    }
}
