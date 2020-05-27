package com.leon.mediamanager.security.services;

import com.leon.mediamanager.models.FileElement;
import com.leon.mediamanager.models.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("organizeFileInfoService")
public class OrganizeFileInfoService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizeFileInfoService.class);

    /**
     * input list of fileInfo from StorageAPI and the client root folder name as "root"
     * the function returns list of element, which is organized with parent and child
     */
    public List<FileElement> constructFileElements(List<FileInfo> fileInfos, String root) {
        List<FileElement> folderElements = new ArrayList<>();
        List<FileElement> fileElements = new ArrayList<>();

        for(FileInfo fileInfo : fileInfos) {
            //modify path to fit required root position
            List<String> pathLst = Arrays.asList(fileInfo.getPath().split("/"));
            for(int i = 0; i < pathLst.size(); i++) {
                if(pathLst.get(i).equals(root)){
                    if(i+1 == pathLst.size()){
                        pathLst = new ArrayList<>();
                    }else{
                        pathLst = pathLst.subList(i+1, pathLst.size());
                    }
                    break;
                }
            }
            String modifiedPath = ".";
            String constructedPath = constructPathString(pathLst);
            if(constructedPath != null){
                modifiedPath = modifiedPath + "/" + constructedPath;
            }

            logger.info("modfied path for folder: {}", modifiedPath);
            // might need to add new folder element
            folderElements = constructFolderElement(modifiedPath, folderElements);
            // set file element
            String modifiedPath_file = modifiedPath + "/" + fileInfo.getName();

            FileElement fileElement = constructFileElement(modifiedPath_file, fileElements, folderElements);
            if(fileElement != null) {
                // have a new File element to add to list
                // add url to element
                fileElement.setUrl(fileInfo.getUrl());
                fileElements.add(fileElement);
            }
        }
        // concat file elements and folder elements
        List<FileElement> resultElements = new ArrayList<>(folderElements);
        resultElements.addAll(fileElements);
        return resultElements;
    }

    public List<FileElement> constructFolderElement(String path, List<FileElement> folderElements) {
        String curFolderName = getFolderName(path);
        if(curFolderName.equals(".")) {
            return folderElements;
        }else{
            // not root so iterate
            List<String> pathLst = Arrays.asList(path.split("/"));
            String curPath = constructPathString(pathLst.subList(0, pathLst.size()-1));
            List<FileElement> newFolderElements = constructFolderElement(curPath, folderElements);

            // Get parent name
            String parentName = pathLst.get(pathLst.size()-1);
            // calculate level of parent
            int parentLevel = pathLst.size() - 2;
            // Find parent element if there is one
            FileElement parentElement = getElement(parentName, parentLevel, newFolderElements);
            // FileElement for curFolderName
            FileElement folderElement = new FileElement();
            // check whether there is already the element for the curFolderName
            if(!checkElement(curFolderName, pathLst.size()-1, newFolderElements)) {
                // supply curFolderName info
                if(parentElement != null) {
                    folderElement.setParent(parentElement.getUuid());
                }else{
                    // null parent element means the parent is root
                    folderElement.setParent(null); // there is no parent as it is root. so no uuid
                }
                folderElement.setName(curFolderName);
                folderElement.setIsFolder(true);
                folderElement.setLevel(pathLst.size()-1);
                // add curFolder to list
                newFolderElements.add(folderElement);
            }
            return newFolderElements;
        }
    }

    public FileElement constructFileElement(String path, List<FileElement> fileElements, List<FileElement> folderElements) {
        List<String> pathLst = Arrays.asList(path.split("/"));
        // get file name and level
        String curFileName = pathLst.get(pathLst.size()-1);
        int curFileLevel = pathLst.size() - 1;
        // get parent element
        FileElement folderElement = getElement(pathLst.get(pathLst.size()-2), pathLst.size()-2, folderElements);
        //check whether file already exists
        if(!checkElement(curFileName, curFileLevel, fileElements)){
            // if there is no file, then create one
            FileElement fileElement = new FileElement();
            if(folderElement == null) {
                //the file is at root
                fileElement.setParent(null);
            }else{
                fileElement.setParent(folderElement.getUuid());
            }
            fileElement.setLevel(curFileLevel);
            fileElement.setIsFolder(false);
            fileElement.setName(curFileName);
            return fileElement;
        }
        return null;

    }


    /**
     * Get parent folder name
     */
    private String getFolderName(String path) {
         String[] names = path.split("/");
         return names[names.length - 1];
    }


    private String constructPathString(List<String> names) {
        String path = null;
        if(names.isEmpty()){
            return null;
        }
        if(names.size() == 1) {
            return names.get(0);
        }else{
            path = names.get(0);
            for(String name : names.subList(1, names.size())) {
                path = path + "/" + name;
            }
            return path;
        }
    }

    private FileElement getElement(String name, Integer level, List<FileElement> elements) {
        if(!elements.isEmpty()) {
            for(FileElement element : elements) {
                if(element.getLevel() == level && element.getName().equals(name)){
                    return element;
                }
            }
        }
        return null;
    }

    private Boolean checkElement(String name, Integer level, List<FileElement> elements) {
        if(!elements.isEmpty()) {
            for(FileElement element : elements) {
                if(element.getName().equals(name) && element.getLevel() == level) {
                    return true;
                }
            }
        }
        return false;
    }

}
