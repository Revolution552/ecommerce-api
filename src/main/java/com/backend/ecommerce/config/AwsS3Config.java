package com.backend.ecommerce.config;



import com.amazonaws.auth.AWSCredentials;

import com.amazonaws.auth.AWSCredentialsProvider;

import com.amazonaws.auth.AWSStaticCredentialsProvider;

import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.client.builder.AwsClientBuilder;

import com.amazonaws.regions.Regions;

import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;



@Configuration

public class AwsS3Config {



    @Value("${aws.s3.accessKey}")

    private String accessKey;



    @Value("${aws.s3.secretKey}")

    private String secretKey;



    @Value("${aws.s3.region}")

    private String region;



    @Bean

    public AmazonS3 s3Client() {

        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);



        return AmazonS3ClientBuilder.standard()

                .withCredentials(credentialsProvider)

                .withRegion(region)

                .build();

    }

}