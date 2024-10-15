package net.engineeringdigest.journalApp.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CompareFacesMatch;
import software.amazon.awssdk.services.rekognition.model.CompareFacesRequest;
import software.amazon.awssdk.services.rekognition.model.CompareFacesResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class RekognitionService {

    private final RekognitionClient rekognitionClient;
    private final AmazonS3 amazonS3;
    private final String bucketName = "pavanprivateprac.dev"; // Your S3 bucket name

    public RekognitionService(RekognitionClient rekognitionClient, AmazonS3 amazonS3) {
        this.rekognitionClient = rekognitionClient;
        this.amazonS3 = amazonS3;
    }

    // Upload the image to S3 for comparison
    public void uploadImageToS3(String key, MultipartFile imageFile) throws IOException {
        amazonS3.putObject(
                new PutObjectRequest(bucketName, key, imageFile.getInputStream(), null)
        );
    }

    public InputStream getImageFromS3(String folder, String imageName) {
        com.amazonaws.services.s3.model.S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, folder + "/" + imageName));
        return s3Object.getObjectContent();
    }

    public List<CompareFacesMatch> compareFaces(String userFolder, MultipartFile uploadedImage, String referenceImageName) throws IOException {
        // Define the folders for reference and uploaded images
        String referenceFolder = userFolder + "/reference";
        String uploadedFolder = userFolder + "/uploaded";

        // Upload the uploaded image to S3 under the 'uploaded' folder
        String uploadedImageKey = uploadedFolder + "/uploaded-image.jpg";
        uploadImageToS3(uploadedImageKey, uploadedImage);

        // Create a reference to the images in S3
        Image referenceImage = Image.builder()
                .s3Object(S3Object.builder()
                        .bucket(bucketName)
                        .name(referenceFolder + "/" + referenceImageName) // Use the reference folder and image name
                        .build())
                .build();

        Image uploadedS3Image = Image.builder()
                .s3Object(S3Object.builder()
                        .bucket(bucketName)
                        .name(uploadedImageKey) // Use the uploaded folder and uploaded image key
                        .build())
                .build();

        // Call AWS Rekognition to compare faces
        CompareFacesRequest request = CompareFacesRequest.builder()
                .sourceImage(referenceImage) // Source: Reference Image
                .targetImage(uploadedS3Image) // Target: Uploaded Image
                .similarityThreshold(70F)  // Adjust the similarity threshold as per your requirement
                .build();

        CompareFacesResponse compareFacesResult = rekognitionClient.compareFaces(request);

        return compareFacesResult.faceMatches();
    }

    // Optionally, download the reference image from S3 to use elsewhere in the app
    public InputStream downloadImageFromS3(String key) {
        com.amazonaws.services.s3.model.S3Object s3Object = amazonS3.getObject(bucketName, key);
        return s3Object.getObjectContent();
    }
}
