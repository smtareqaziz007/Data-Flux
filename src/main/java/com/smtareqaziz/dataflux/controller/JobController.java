package com.smtareqaziz.dataflux.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/launch")
@RequiredArgsConstructor
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job job;

    @PostMapping
    public void launchJob(@RequestParam String fileName) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileName", fileName)
                .addLong("startAt" , System.currentTimeMillis())
                .toJobParameters();

        // another way to achieve this by configuring JobLauncher and assign an TaskExecutor to it
        CompletableFuture.runAsync(()-> {
            try {
                jobLauncher.run(job ,jobParameters);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
