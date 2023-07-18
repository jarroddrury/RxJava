/*
 * Copyright (c) 2015-present, RxJava Contributors.
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

package io.reactivex.rxjava3.core;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * Interface to compose {@link Single}s.
 *
 * @param <Upstream> the upstream value type
 * @param <Downstream> the downstream value type
 */
@FunctionalInterface
public interface SingleTransformer<@NonNull Upstream, @NonNull Downstream> {
    /**
     * Applies a function to the upstream {@link Single} and returns a {@link SingleSource} with
     * optionally different element type.
     * @param upstream the upstream {@code Single} instance
     * @return the transformed {@code SingleSource} instance
     */
    @NonNull
    SingleSource<Downstream> apply(@NonNull Single<Upstream> upstream);
}
