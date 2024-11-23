package com.smtareqaziz.dataflux.config;

import com.smtareqaziz.dataflux.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

//    @Bean
//    public FlatFileItemReader<Customer> itemReader(){
//
//    }
}
