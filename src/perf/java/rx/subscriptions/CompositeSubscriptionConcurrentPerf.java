/**
 * Copyright 2014 Netflix, Inc.
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

package rx.subscriptions;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import rx.Subscription;

/**
 * Benchmark typical composite subscription.
 * <p>
 * gradlew benchmarks "-Pjmh=-f 1 -tu s -bm thrpt -wi 5 -i 5 -r 1 .*CompositeSubscriptionConcurrentPerf.*"
 * <p>
 * gradlew benchmarks "-Pjmh=-f 1 -tu ns -bm avgt -wi 5 -i 5 -r 1 .*CompositeSubscriptionConcurrentPerf.*"
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Threads(2)
@State(Scope.Group)
public class CompositeSubscriptionConcurrentPerf {
    @Param({ "1", "1000", "1000000" })
    public int loop;
    
    public final CompositeSubscription csub = new CompositeSubscription();
    @Param({ "1", "5", "10" })
    public int count;
    
    public Subscription[] values;
    @Setup
    public void setup() {
        values = new Subscription[count];
        for (int i = 0; i < count; i++) {
            values[i] = new Subscription() {
                @Override
                public boolean isUnsubscribed() {
                    return false;
                }
                @Override
                public void unsubscribe() {
                    
                }
            };
        }
    }

    final Subscription sub = new Subscription() {
        @Override
        public boolean isUnsubscribed() {
            return false;
        }
        @Override
        public void unsubscribe() {
            
        }
    };
    @Group("tpt")
    @GroupThreads(1)
    @Benchmark
    public void simpleAddRemoveOne() {
        CompositeSubscription csub = this.csub;
        Subscription[] values = this.values;
        
        for (int i = loop; i > 0; i--) {
            for (int j = values.length - 1; j >= 0; j--) {
                csub.add(values[j]);
            }
            for (int j = values.length - 1; j >= 0; j--) {
                csub.remove(values[j]);
            }
        }
    }
    @Group("tpt")
    @GroupThreads(1)
    @Benchmark
    public void simpleAddRemoveOne2() {
        CompositeSubscription csub = this.csub;
        Subscription[] values = this.values;
        
        for (int i = loop; i > 0; i--) {
            for (int j = values.length - 1; j >= 0; j--) {
                csub.add(values[j]);
            }
            for (int j = values.length - 1; j >= 0; j--) {
                csub.remove(values[j]);
            }
        }
    }
}
