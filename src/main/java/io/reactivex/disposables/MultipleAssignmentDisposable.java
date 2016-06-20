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

package io.reactivex.disposables;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.internal.disposables.*;

public final class MultipleAssignmentDisposable implements Disposable {
    final AtomicReference<Disposable> resource;
    
    public MultipleAssignmentDisposable() {
        this.resource = new AtomicReference<Disposable>();
    }
    
    public MultipleAssignmentDisposable(Disposable initialDisposable) {
        this.resource = new AtomicReference<Disposable>(initialDisposable);
    }
    
    public void set(Disposable d) {
        DisposableHelper.replace(resource, d);
    }
    
    public Disposable get() {
        return resource.get();
    }
    
    @Override
    public void dispose() {
        DisposableHelper.dispose(resource);
    }
    
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(get());
    }
}
