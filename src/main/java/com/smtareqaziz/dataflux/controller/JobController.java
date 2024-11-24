package com.smtareqaziz.dataflux.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/launch")
@RequiredArgsConstructor
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job job;

    @PostMapping
    public void launchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt" , System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
    }
}
