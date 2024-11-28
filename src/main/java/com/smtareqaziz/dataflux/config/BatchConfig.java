package com.smtareqaziz.dataflux.config;

import com.smtareqaziz.dataflux.entity.Customer;
import com.smtareqaziz.dataflux.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    private final PlatformTransactionManager platformTransactionManager;
    private final JobRepository jobRepository;
    private final CustomerRepository customerRepository;

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> itemReader(@Value("#{jobParameters['fileName']}") String fileName) {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
        //NOTE: ClassPathResource automatically looks in the resource folder without defining the relative path
        reader.setResource(new ClassPathResource(fileName));
        reader.setName("csvReader");
        reader.setLinesToSkip(1);
        reader.setLineMapper(lineMapper());
        return reader;
    }

    @Bean
    public CustomerProcessor itemProcessor(){
        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> itemWriter(){
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step getStep(){
        return new StepBuilder("ioStep" , jobRepository)
                .<Customer, Customer>chunk(1000, platformTransactionManager)
                .reader(itemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .taskExecutor(getTaskExecutor())
                .build();
    }

    @Bean
    public Step getTasklet(){
        return new StepBuilder("tasklet" , jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("----------Tasklet step started------------");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .build();
    }

    @Bean
    public Job getJob(){
        return new JobBuilder("readWriteJob" , jobRepository)
                .start(getStep()) // TODO: See if I can run another step in parallel & conditional steps
                .next(getTasklet())
                .build();
    }

    @Bean(name = "customJobLauncher")
    public TaskExecutorJobLauncher getJobLauncher() throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setTaskExecutor(getTaskExecutor());
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public TaskExecutor getTaskExecutor(){
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(30);
        return taskExecutor;
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "phone", "address", "country", "dateOfBirth");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }
}
