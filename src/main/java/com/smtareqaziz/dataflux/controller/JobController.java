package com.smtareqaziz.dataflux.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/launch")
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job job;

    public JobController(@Qualifier("customJobLauncher") JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }


    @PostMapping
    public void launchJob(@RequestParam String fileName) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileName", fileName)
                .addLong("startAt" , System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(job ,jobParameters);
    }
}
