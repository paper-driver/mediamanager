package com.leon.mediamanager.controllers;

import com.leon.mediamanager.models.FileInfo;
import com.leon.mediamanager.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/storage")
public class StorageController {

    @Autowired
    @Qualifier("customRestTemplate")
    RestTemplate restTemplate;

    @Value("${downstream.storageapi}")
    String storageApiUrl;

    @PostMapping("/upload")
    public ResponseEntity<MessageResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // convert file to Resource
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        // set RequestEntity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        // send request
        return restTemplate.exchange(storageApiUrl + "/api/mm/upload", HttpMethod.POST, requestEntity, MessageResponse.class);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> getListFiles() {
        FileInfo[] fileInfos = restTemplate.getForObject(storageApiUrl + "/api/mm/files", FileInfo[].class);
        return ResponseEntity.ok().body(Arrays.asList(fileInfos));
    }

    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        return restTemplate.getForEntity(storageApiUrl + "/api/mm/file/" + filename, Resource.class);
    }

}

class FileInfoList {
    private List<FileInfo> fileInfos;

    void FIleInfoList() {
        fileInfos = new ArrayList<>();
    }

    void FileInfoList(List<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
    }

    List<FileInfo> getFileInfos() {
        return fileInfos;
    }

    void setFileInfos(List<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
    }
}
