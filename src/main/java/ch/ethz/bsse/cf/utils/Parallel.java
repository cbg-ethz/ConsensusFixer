/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.ethz.bsse.cf.utils;

/**
 * Java Parallel.For Parallel.ForEach
 */
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


import java.util.concurrent.ExecutionException;

/**
 * A Java Paralle.For | Parallel.ForEach
 */
public class Parallel {

    static int iCPU = Runtime.getRuntime().availableProcessors();

    public static <T> void ForEach(Iterable<T> parameters, final LoopBody<T> loopBody) {
        ExecutorService executor = Executors.newFixedThreadPool(iCPU);
        List<Future<?>> futures = new LinkedList<>();

        for (final T param : parameters) {
            Future<?> future = executor.submit(new Runnable() {
                public void run() {
                    loopBody.run(param);
                }
            });

            futures.add(future);
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }

        executor.shutdown();
    }
}