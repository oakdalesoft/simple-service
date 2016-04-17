package com.oakdale.demo.simpleservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Alex on 15/04/2016.
 */

@Controller
public class SimpleServiceController {

    private final AtomicLong counter = new AtomicLong();

    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    private final Logger log = LoggerFactory.getLogger(SimpleServiceController.class);

    private final Map<Long, JobStatus> completedJobs = new ConcurrentHashMap<>();

    @RequestMapping(path = "/submit", method = RequestMethod.GET)
    public @ResponseBody JobStatus submitRequest(@RequestParam(value="name", required=false, defaultValue="anonymous") String name,
                                                 @RequestParam(value="throw", required=false, defaultValue="null") String exception) {

        JobStatus status = new JobStatus(counter.incrementAndGet(), JobStatus.Status.Requested, name);
        this.completedJobs.put(status.getId(), status);

        CompletableFuture.supplyAsync(() -> {
            this.log.info("Submitted job to run on thread [{}] with id [{}] from [{}]", Thread.currentThread().getName(), status.getId(), status.getOriginator());
            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {}

            if(!"null".equals(exception)) {
                throw new RuntimeException(exception); // error in processing
            }

            status.setResults(String.format("Results from job completed on thread [{%s}]", Thread.currentThread().getName()));
            return status;
        }, this.pool)
                .handle((done, ex) -> {
                    if(done != null) {
                        done.setStatus(JobStatus.Status.Completed);
                        this.completedJobs.put(status.getId(), done);
                        this.log.info("Completed job on thread [{}] with id [{}]", Thread.currentThread().getName(), status.getId());
                        return done;
                    } else {
                        status.setStatus(JobStatus.Status.Failed);
                        status.setResults(ex.toString());
                        this.completedJobs.put(status.getId(), status);
                        this.log.info("Failed job on thread [{}] with id [{}]", Thread.currentThread().getName(), status.getId());
                        return status;
                    }
                });

        return status;
    }

    @RequestMapping(path = "/get", method = RequestMethod.GET)
    public @ResponseBody JobStatus getRequest(@RequestParam(value="id", required=true, defaultValue = "0") String id) {
        return this.completedJobs.get(Long.decode(id));
    }

}
