package net.engineeringdigest.journalApp.Controllers;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import net.engineeringdigest.journalApp.Service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/public/s3bucket")
public class S3Controller {

    @Autowired
    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    // 1. Upload endpoint
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = s3Service.uploadFile(file);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }
    @PostMapping("/upload-to-folder")
    public ResponseEntity<String> uploadFileToFolder(@RequestParam("file") MultipartFile file, String folder) {
        try {
            String fileName = s3Service.uploadFileToFolder(file,folder);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }

    // 2. Generate presigned URL for downloading
    @GetMapping("/download-url/{fileName}")
    public ResponseEntity<String> generateDownloadUrl(@PathVariable String fileName) {
        URL presignedUrl = s3Service.generatePresignedUrl(fileName);
        return ResponseEntity.status(HttpStatus.OK).body(presignedUrl.toString());
    }
    @GetMapping("/presigned-url")
    public ResponseEntity<String> generatePreSignedUrl(@RequestParam("filePath") String filePath) {
        String preSignedUrl = s3Service.generatePreSignedUrl(filePath);
        return ResponseEntity.ok(preSignedUrl);
    }

    // 3. Download and stream file content from S3
    @GetMapping("/download-stream/{fileName}")
    public ResponseEntity<byte[]> downloadFileAsStream(@PathVariable String fileName) {
        try {
            InputStream inputStream = s3Service.downloadFileAsStream(fileName);
            byte[] fileContent = inputStream.readAllBytes();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/list")
    public ResponseEntity<List<String>> listAllObjects() {
        List<String> objectKeys = s3Service.listAllObjects();
        return ResponseEntity.status(HttpStatus.OK).body(objectKeys);
    }

    // Delete a file
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("fileName") String fileName) {
        s3Service.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully: " + fileName);
    }

    // Copy a file
    @PostMapping("/copy")
    public ResponseEntity<String> copyFile(@RequestParam("sourceKey") String sourceKey,@RequestParam("destinationKey") String destinationKey) {
        s3Service.copyObject(sourceKey, destinationKey);
        return ResponseEntity.ok("File copied successfully from " + sourceKey + " to " + destinationKey);
    }

    // Rename a file
    @PostMapping("/rename")
    public ResponseEntity<String> renameFile(@RequestParam("oldKey") String oldKey,
                                             @RequestParam("newKey") String newKey) {
        s3Service.renameFile(oldKey, newKey);
        return ResponseEntity.ok("File renamed successfully from " + oldKey + " to " + newKey);
    }

    // Check if a file exists
    @GetMapping("/exists")
    public ResponseEntity<String> doesObjectExist(@RequestParam("fileName") String fileName) {
        boolean exists = s3Service.doesObjectExist(fileName);
        return exists ? ResponseEntity.ok("File exists: " + fileName)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + fileName);
    }

    // Get metadata of a file
    @GetMapping("/metadata")
    public ResponseEntity<ObjectMetadata> getObjectMetadata(@RequestParam("fileName") String fileName) {
        ObjectMetadata metadata = s3Service.getObjectMetadata(fileName);
        return ResponseEntity.ok(metadata);
    }

    // List all buckets in the account
    @GetMapping("/buckets")
    public ResponseEntity<List<Bucket>> listAllBuckets() {
        List<Bucket> buckets = s3Service.listAllBuckets();
        return ResponseEntity.ok(buckets);
    }

    // List all files in a specific bucket (folder)
    @GetMapping("/list-files")
    public ResponseEntity<List<String>> listFilesInFolder(@RequestParam("folder") String folder) {
        List<String> files = s3Service.listFilesInFolder(folder);
        return ResponseEntity.ok(files);
    }


}