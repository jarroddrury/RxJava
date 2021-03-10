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

package io.reactivex.rxjava3.internal.jdk8;

import io.reactivex.rxjava3.annotations.NonNull;

import java.util.NoSuchElementException;

/**
 * Signals the only element of the source via the underlying CompletableFuture,
 * signals the a default item if the upstream is empty or signals {@link IllegalArgumentException}
 * if the upstream has more than one item.
 *
 * @param <T> the element type
 * @since 3.0.0
 */
public final class ObservableSingleStageObserver<T> extends ObservableStageObserver<T> {

    final boolean hasDefault;

    final T defaultItem;

    public ObservableSingleStageObserver(boolean hasDefault, T defaultItem) {
        this.hasDefault = hasDefault;
        this.defaultItem = defaultItem;
    }

    @Override
    public void onNext(@NonNull T t) {
        if (value != null) {
            value = null;
            completeExceptionally(new IllegalArgumentException("Sequence contains more than one element!"));
        } else {
            value = t;
        }
    }

    @Override
    public void onComplete() {
        if (!isDone()) {
            T v = value;
            clear();
            if (v != null) {
                complete(v);
            } else if (hasDefault) {
                complete(defaultItem);
            } else {
                completeExceptionally(new NoSuchElementException());
            }
        }
    }
}
