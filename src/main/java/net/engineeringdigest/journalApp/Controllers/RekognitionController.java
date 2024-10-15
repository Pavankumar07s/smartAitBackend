package net.engineeringdigest.journalApp.Controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import net.engineeringdigest.journalApp.Service.RekognitionService;
import software.amazon.awssdk.services.rekognition.model.CompareFacesMatch;

import java.util.List;

@RestController
@RequestMapping("/api/public/rekognition")
public class RekognitionController {

    private final RekognitionService rekognitionService;

    public RekognitionController(RekognitionService rekognitionService) {
        this.rekognitionService = rekognitionService;
    }

    @PostMapping("/compare")
    public String compareImages(
            @RequestParam("folder") String userFolder,
            @RequestParam("file") MultipartFile uploadedImage,
            @RequestParam("referenceImage") String referenceImageName) throws Exception {

        List<CompareFacesMatch> faceMatches = rekognitionService.compareFaces(userFolder, uploadedImage, referenceImageName);

        if (faceMatches.isEmpty()) {
            return "No matching faces found!";
        } else {
            return "Matching faces found with confidence: " + faceMatches.get(0).similarity() + "%";
        }
    }
}
