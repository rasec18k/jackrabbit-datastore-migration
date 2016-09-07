/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.woonsan.jackrabbit.migration.datastore.batch;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.ListItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public ItemReader<String> reader() {
        List<String> list = new LinkedList<>();
        for (int i = 0; i < 25; i++) {
            list.add("Hello-" + i);
        }

        ListItemReader<String> reader = new ListItemReader<>(list);
        return reader;
    }

    @Bean
    public ItemProcessor<String, String> processor() {
        return new ItemProcessor<String, String>() {
            @Override
            public String process(String item) throws Exception {
                return "[Processed ] " + item;
            }
        };
    }

    @Bean
    public ItemWriter<String> writer() {
        ListItemWriter<String> writer = new ListItemWriter<>();
        return writer;
    }

    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobCompletionNotificationListener();
    }

    @Bean
    public EntryItemListener itemListener() {
        return new EntryItemListener();
    }

    @Bean
    public Job testJob() {
        return jobBuilderFactory.get("testJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener())
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<String, String> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .listener((ItemWriteListener<String>) itemListener())
                .build();
    }
}