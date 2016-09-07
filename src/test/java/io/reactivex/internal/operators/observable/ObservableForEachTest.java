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

package io.reactivex.internal.operators.observable;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.exceptions.TestException;
import io.reactivex.functions.*;

public class ObservableForEachTest {

    @Test
    public void forEachWile() {
        final List<Object> list = new ArrayList<Object>();

        Observable.range(1, 5)
        .doOnNext(new Consumer<Integer>() {
            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        })
        .forEachWhile(new Predicate<Integer>() {
            @Override
            public boolean test(Integer v) throws Exception {
                return v < 3;
            }
        });

        assertEquals(Arrays.asList(1, 2, 3), list);
    }

    @Test
    public void forEachWileWithError() {
        final List<Object> list = new ArrayList<Object>();

        Observable.range(1, 5).concatWith(Observable.<Integer>error(new TestException()))
        .doOnNext(new Consumer<Integer>() {
            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        })
        .forEachWhile(new Predicate<Integer>() {
            @Override
            public boolean test(Integer v) throws Exception {
                return true;
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                list.add(100);
            }
        });

        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

}
