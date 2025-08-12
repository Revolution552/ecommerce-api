package com.backend.ecommerce.products.product.service;



import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.services.s3.model.DeleteObjectRequest;

import com.amazonaws.services.s3.model.ObjectMetadata;

import com.amazonaws.services.s3.model.PutObjectRequest;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.UUID;



@Service

public class ImageService {



    private final AmazonS3 s3Client;



    @Value("${aws.s3.bucketName}")

    private String bucketName;



    public ImageService(AmazonS3 s3Client) {

        this.s3Client = s3Client;

    }



    /**

     * Uploads a MultipartFile to the S3 bucket.

     *

     * @param file The file to upload.

     * @return The URL of the uploaded file.

     */

    public String uploadImage(MultipartFile file) {

        if (file.isEmpty()) {

            throw new IllegalStateException("Cannot upload empty file.");

        }



        try {

// Generate a unique file name to avoid collisions

            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();



// Set metadata for the S3 object

            ObjectMetadata metadata = new ObjectMetadata();

            metadata.setContentType(file.getContentType());

            metadata.setContentLength(file.getSize());



// Create a PutObjectRequest to upload the file

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata);



// Upload the file to S3

            s3Client.putObject(putObjectRequest);



// Return the public URL of the uploaded file

            return s3Client.getUrl(bucketName, fileName).toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3.", e);
        }
    }
    /**

     * Deletes an image from the S3 bucket using its URL.
     *
     * @param imageUrl The URL of the image to delete.
     */
    public void deleteImage(String imageUrl) {
// Extract the file name from the URL
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
// Create a DeleteObjectRequest
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fileName);
// Delete the object from S3
        s3Client.deleteObject(deleteObjectRequest);
    }
}