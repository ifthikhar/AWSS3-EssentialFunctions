package com.s3functionality.microservices.currencyexchangeservice.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class AWSS3Service {

    @Autowired
    AmazonClient amazonClient;
    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;


    public String uploadFileTos3bucket(String fileName, File file, String bucketName) {
        String fileUrl = "";
        fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
        amazonClient.s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        file.delete();
        return fileUrl;
    }

    public String deleteFileFromS3Bucket(String fileUrl, String bucketName) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        System.out.println("fileurl is " + fileName + "filename is " + fileName);

        try {
            amazonClient.s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        }
        catch (AmazonServiceException ex) {
            System.out.println("error [" + ex.getMessage() + "] occurred while removing [" + fileName + "] ");
        }
        return "Successfully deleted";
    }
}
