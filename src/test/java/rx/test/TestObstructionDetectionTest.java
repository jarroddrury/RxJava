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
package rx.test;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.*;

import rx.*;
import rx.Scheduler.Worker;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.test.TestObstructionDetection.ObstructionException;

public class TestObstructionDetectionTest {
    private static Scheduler.Worker w;
    @AfterClass
    public static void afterClass() {
        Worker w2 = w;
        if (w2 != null) {
            w2.unsubscribe();
        }
    }
    @After
    public void after() {
        TestObstructionDetection.checkObstruction();
    }
    @Test(timeout = 10000, expected = ObstructionException.class)
    public void testObstruction() {
        Scheduler.Worker w = Schedulers.computation().createWorker();
        
        final AtomicReference<Thread> t = new AtomicReference<Thread>();
        try {
            w.schedule(new Action0() {
                @Override
                public void call() {
                    t.set(Thread.currentThread());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        
                    }
                }
            });
            TestObstructionDetection.checkObstruction();
        } finally {
            Thread t0 = t.get();
            if (t0 != null) {
                t0.interrupt();
            }
            w.unsubscribe();
        }
    }
    @Test(timeout = 10000)
    public void testNoObstruction() {
        w = Schedulers.computation().createWorker();
        
        w.schedule(new Action0() {
            @Override
            public void call() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    
                }
            }
        });
        rx.test.TestObstructionDetection.checkObstruction();
    }
}
