package net.engineeringdigest.journalApp.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;
    private final String bucketName = "pavanprivateprac.dev"; // You can fetch this from application.yml

    @Autowired
    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    // 1. Upload a file to S3
    public String uploadFile(MultipartFile file) throws IOException {
        File convertedFile = convertMultipartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, convertedFile));
        convertedFile.delete();  // Clean up after uploading
        return fileName;
    }

    public String uploadFileToFolder(MultipartFile file, String folderName) throws IOException {
        // Convert the MultipartFile to a File
        File convertedFile = convertMultipartFileToFile(file);

        // Ensure the folder name ends with a "/"
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }

        // Generate the unique file name
        String fileName = folderName + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Upload the file to the specific folder in S3
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, convertedFile));

        // Clean up local file after upload
        convertedFile.delete();

        return fileName;
    }

    // Helper method to convert MultipartFile to File
    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        }
        return convertedFile;
    }

    // 2. Generate a presigned URL for downloading
    public URL generatePresignedUrl(String fileName) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60; // URL valid for 1 hour
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileName)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }

    // Generate PreSigned URL
    public String generatePreSignedUrl(String filePath) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;  // 1 hour
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, filePath)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        System.out.println(generatePresignedUrlRequest);
        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();  // Return URL as a string
    }

    // 3. Stream the object directly from S3
    public InputStream downloadFileAsStream(String fileName) {
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, fileName));
        return s3Object.getObjectContent();
    }

    // Method to list all objects in the S3 bucket
    public List<String> listAllObjects() {
        ObjectListing objectListing = amazonS3.listObjects(bucketName);
        List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();

        // Map the object summaries to a list of file names
        return summaries.stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    // Deletes an object from S3 bucket
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(bucketName, fileName);
    }

    // Copies an object from one location to another in the same or different bucket
    public void copyObject(String sourceKey, String destinationKey) {
        amazonS3.copyObject(bucketName, sourceKey, bucketName, destinationKey);
    }

    // Renames an object by copying it to a new key and deleting the original
    public void renameFile(String oldKey, String newKey) {
        copyObject(oldKey, newKey);
        deleteFile(oldKey);
    }

    // Check if an object exists in the S3 bucket
    public boolean doesObjectExist(String fileName) {
        return amazonS3.doesObjectExist(bucketName, fileName);
    }

    // Get object metadata (size, content type, etc.) for a given file in S3
    public ObjectMetadata getObjectMetadata(String fileName) {
        return amazonS3.getObjectMetadata(bucketName, fileName);
    }

    // List all S3 buckets in your account
    public List<Bucket> listAllBuckets() {
        return amazonS3.listBuckets();
    }

    // List files in a specific folder
    public List<String> listFilesInFolder(String folderName) {
        ObjectListing objectListing = amazonS3.listObjects(bucketName, folderName);
        List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        List<String> fileNames = new ArrayList<>();
        for (S3ObjectSummary objectSummary : objectSummaries) {
            fileNames.add(objectSummary.getKey());
        }
        return fileNames;
    }

    // Get image from S3 as InputStream (using AmazonS3 SDK v1)
    public InputStream getImageFromS3(String userFolder, String imageName) {
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, userFolder + "/" + imageName));
        return s3Object.getObjectContent();
    }



}
