package com.leon.mediamanager.controllers;
import com.leon.mediamanager.models.*;
import com.leon.mediamanager.payload.response.MessageResponse;
import com.leon.mediamanager.repository.ConfirmationTokenRepository;
import com.leon.mediamanager.repository.RoleRepository;
import com.leon.mediamanager.repository.UserRepository;
import com.leon.mediamanager.security.services.EmailSenderService;
import com.leon.mediamanager.security.services.OrganizeFileInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import sun.plugin2.message.Message;


import javax.validation.Valid;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    EmailSenderService emailSenderService;

    @Autowired
    OrganizeFileInfoService organizeFileInfoService;

    @Autowired
    PasswordEncoder encoder;

//    @Autowired
//    OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    @Qualifier("customRestTemplate")
    RestTemplate restTemplate;

    @Value("${email.address}")
    String emailAddress;

    @Value("${downstream.storageapi}")
    String storageApiUrl;

    @GetMapping("/roleconfirmation")
    public ResponseEntity<?> approveRoleUpdate(@RequestParam("token")String token){
        logger.info("approving role update for: {}", token);
        /* get stored pending token */
        ConfirmationToken pendingToken = confirmationTokenRepository.findByToken(token);

        if(pendingToken != null){
            /* verify the expiry date of pending token */
            Calendar calendar = Calendar.getInstance();
            if((pendingToken.getExpiryDate().getTime()-calendar.getTime().getTime())<=0){
                /* delete token and request */
                confirmationTokenRepository.delete(pendingToken);
                confirmationTokenRepository.flush();

                return ResponseEntity.ok(new MessageResponse("Bad Request: Token is expired."));
            }

            User user = userRepository.findById(pendingToken.getId())
                    .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            logger.warn("requested roles: {}", pendingToken.getRoles().toString());
            /* organize requested roles */
            Set<Role> requestedRole = new HashSet<>();
            for(Role role: pendingToken.getRoles()){
                Role actualRole = roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                requestedRole.add(actualRole);
            }
            user.setRoles(requestedRole);
            /* save updated roles */
            logger.warn("save User table");
            userRepository.saveAndFlush(user);

            /* delete used confirmation token */
            confirmationTokenRepository.delete(pendingToken);
            confirmationTokenRepository.flush();

            if(confirmationTokenRepository.findByToken(token) == null){
                logger.info("successfully delete the used token");
            }

            return ResponseEntity.ok(new MessageResponse("Approved."));
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("Error: no such token."));
        }
    }

    @GetMapping("/rolerejection")
    public ResponseEntity<?> rejectRoleUpdate(@RequestParam("token")String token){
        logger.info("rejecting role update for: {}", token);
        /* get stored pending token */
        ConfirmationToken pendingToken = confirmationTokenRepository.findByToken(token);

        /* delete used confirmation token */
        confirmationTokenRepository.delete(pendingToken);
        confirmationTokenRepository.flush();

        if(confirmationTokenRepository.findByToken(token) == null){
            logger.info("successfully delete the used token");
        }

        return ResponseEntity.ok(new MessageResponse("Rejected."));
    }

    @GetMapping("/check-storage-api")
    public ResponseEntity<?> checkTemp() {
        String  msg = restTemplate.getForObject(storageApiUrl + "/api/central/access", String.class);
        logger.info("Got the response of /check-storage-api: {}", msg);
        return ResponseEntity.ok(msg);
//        return internalRestService.checkStorageApi();
    }

    @PostMapping("/uploadfolder")
    public ResponseEntity<FileElement> uploadFolder(@Valid @RequestBody FileElement folderElement) {
        ResponseEntity<MessageResponse> result = restTemplate.getForEntity(
                storageApiUrl + "/api/mm/uploadfolder&folderpath=" + folderElement.getFullPath(), MessageResponse.class);
        if(result.getStatusCode().is2xxSuccessful()){
            FileElement officialFolderElement = new FileElement();
            officialFolderElement.setFullPath(folderElement.getFullPath());
            officialFolderElement.setName(folderElement.getName());
            officialFolderElement.setIsFolder(true);
            officialFolderElement.setLevel(folderElement.getLevel());
            officialFolderElement.setParent(folderElement.getParent());
            // reply Folder element with generated uuid
            return ResponseEntity.ok().body(officialFolderElement);
        }else{
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }

    @GetMapping("/files/{foldername:.+}")
    public ResponseEntity<List<FileElement>> getListFiles(@PathVariable String foldername) {
        FileInfo[] fileInfos = restTemplate.getForObject(storageApiUrl + "/api/mm/files/" + foldername, FileInfo[].class);
        // change url of each file to go through mediamanager
        for (FileInfo fileInfo : fileInfos) {
            fileInfo.setUrl(MvcUriComponentsBuilder
                    .fromMethodName(StorageController.class, "getFile", fileInfo.getUrl()).build().toString());
        }
        // create organized parent child relationed file infos
        List<FileElement> fileElements = organizeFileInfoService.constructFileElements(Arrays.asList(fileInfos), foldername);
        return ResponseEntity.ok().body(fileElements);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> getListFiles() {
        FileInfo[] fileInfos = restTemplate.getForObject(storageApiUrl + "/api/mm/files", FileInfo[].class);
        // change url of each file to go through mediamanager
        for (FileInfo fileInfo : fileInfos) {
            logger.info(fileInfo.getUrl());
            fileInfo.setUrl(MvcUriComponentsBuilder
                    .fromMethodName(StorageController.class, "getFile", fileInfo.getUrl()).build().toString());
        }
        return ResponseEntity.ok().body(Arrays.asList(fileInfos));
    }
}
