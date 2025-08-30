package com.ringme.cms.common;

import com.ringme.cms.config.AppConfiguration;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Log4j2
@Component
public class UploadFile {
    @Autowired
    AppConfiguration appConfiguration;


    @Deprecated
    public void upload(MultipartFile image, String[] fileName) {
        try {
            Path ROOT_FOLDER = Paths.get(appConfiguration.getRootPath());
            log.info("ROOT_FOLDER|" + ROOT_FOLDER);
            if(fileName == null)
                return;
            if (!Files.exists(ROOT_FOLDER.resolve(fileName[0]))) {
                Files.createDirectories(ROOT_FOLDER.resolve(fileName[0]));
            }
            Path file = ROOT_FOLDER.resolve(fileName[1]);
            try (OutputStream os = Files.newOutputStream(file)) {
                os.write(image.getBytes());
            }
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
    }
    @Deprecated
    public String[] saveContractFile(MultipartFile image, String type){
        try {
            log.info("appConfiguration|" + appConfiguration.getFileInDBPrefix());
            if(image == null)
                return null;
            String originalFilename = image.getOriginalFilename();
            if(originalFilename == null || originalFilename.equals(""))
                return null;
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if(fileExtension == null || fileExtension.isEmpty())
                return null;
            String fileName = Helper.generateRandomString(32);
            String time = Helper.getTimeNow();
            Path staticPath = Paths.get(appConfiguration.getFileInDBPrefix());
            log.info("staticPath|" + staticPath);
            Path imagePath = Paths.get(type + "/" + time);
            String[] file = new String[3];
            file[0] = staticPath.resolve(imagePath).toString().replaceAll("\\\\", "/");
            file[1] = staticPath.resolve(imagePath).resolve(fileName + "." + fileExtension).toString().replaceAll("\\\\", "/");
            file[2] = originalFilename;
            return file;
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }

    public Path createImageFile(String thumbUpload, String type) {
        try {
            if(thumbUpload == null || thumbUpload.equals(""))
                return null;
            String[] dataArray = thumbUpload.trim().split(",");
            String imgBase64 = "";
            String fileExtension = "jpg";
            if (dataArray.length > 1) {
                imgBase64 = dataArray[1];
                fileExtension = dataArray[0].replace("data:image/", "").replace(";base64", "");
            } else {
                imgBase64 = thumbUpload;
            }
            String fileName = Helper.generateRandomString(32);
            Path timePath = Helper.getPathByTime();
            Path relativePath = Paths.get("uploads/" + appConfiguration.getFileInDBPrefix()).resolve(type).resolve(timePath);
            relativePath = relativePath.resolve(fileName + "." + fileExtension);
            Path obsoluteSavePath = Paths.get(appConfiguration.getRootPath()).resolve(relativePath);
            Path folderParent = obsoluteSavePath.getParent();
            if(!Files.exists(folderParent)) {
                Files.createDirectories(folderParent);
            }
            log.info("PATH|" + "|relativePath = " + relativePath + "|obsolutePath = " + obsoluteSavePath);

            try (OutputStream os = Files.newOutputStream(obsoluteSavePath)) {
                os.write(Base64.getDecoder().decode(imgBase64));
                os.flush();
            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
            }
            return relativePath;
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }

    public Path createAllTypeFile(MultipartFile multipartFile, String type) {
        try {
            if (multipartFile == null || multipartFile.isEmpty()) {
                return null;
            }

            String originalFileName = multipartFile.getOriginalFilename();
            String fileExtension = "wav";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
            }

            String fileName = Helper.generateRandomString(32);
            Path timePath = Helper.getPathByTime();
            Path relativePath = Paths.get("uploads/" + appConfiguration.getFileInDBPrefix())
                    .resolve(type)
                    .resolve(timePath)
                    .resolve(fileName + "." + fileExtension);
            Path absoluteSavePath = Paths.get(appConfiguration.getRootPath()).resolve(relativePath);

            Path folderParent = absoluteSavePath.getParent();
            if (!Files.exists(folderParent)) {
                Files.createDirectories(folderParent);
            }

            log.info("PATH|relativePath = " + relativePath + "|absolutePath = " + absoluteSavePath);

            try (InputStream inputStream = multipartFile.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(absoluteSavePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
            }

            return relativePath;
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }


    public Path createImageFileV2(String imgBase64Data, String type) {
        try {
            if(imgBase64Data == null || imgBase64Data.equals(""))
                return null;
            String[] dataArray = imgBase64Data.trim().split(",");
            String imgBase64 = "";
            String fileExtension = "jpg";
            if (dataArray.length > 1) {
                imgBase64 = dataArray[1];
                fileExtension = dataArray[0].replace("data:image/", "").replace(";base64", "");
            } else {
                imgBase64 = imgBase64Data;
            }
            String fileName = Helper.generateRandomString(32);

            Path pathOnFTPServer = Paths.get(appConfiguration.getFtpImageDirectory()).resolve(type).resolve(Helper.getTimeNow());

            pathOnFTPServer = pathOnFTPServer.resolve(fileName + "." + fileExtension);

            log.info("PATH|" + "|relativePath = " + pathOnFTPServer);

            try {
//                uploadFTPByte(imgBase64, pathOnFTPServer, timePath, type);
                uploadBase64Img2FTP(imgBase64, pathOnFTPServer);
            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
            }
            return pathOnFTPServer;

        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }

    private void uploadBase64Img2FTP(String imgBase64, Path pathOnFTPServer) {
        log.info("relativePath|" + pathOnFTPServer.toString());
        String serverDirectory = pathOnFTPServer.toString();

//        String[] pathSegments = pathOnFTPServer.toString().split(File.separator);
//        StringBuilder currentPath = new StringBuilder();

        String FTP_ADDRESS = appConfiguration.getFTPAddress();
        String LOGIN = appConfiguration.getFTPLogin();
        String PSW = appConfiguration.getFTPPWS();
        final FTPClient con = new FTPClient();

        try {
            con.connect(FTP_ADDRESS);
            if (con.login(LOGIN, PSW)) {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);
                log.info("FTP ==> current directory: {}", con.printWorkingDirectory());
                Path parentPath = pathOnFTPServer.getParent();
                log.info("FTP ==> parentPath: {}", parentPath.toString());
                parentPath.forEach(p -> {
                    String folder = p.toString();

                    try {
                        if(!con.changeWorkingDirectory(folder)) {
                            con.makeDirectory(folder);
                            con.changeWorkingDirectory(folder);
                        }
                    } catch (Exception ex) {
                        log.error("Cannot create folder : {}", folder);
                    }
                });


                byte[] imgBytes = Base64.getDecoder().decode(imgBase64);
                InputStream inputStream = new ByteArrayInputStream(imgBytes);
                log.info("Server directory|{}", serverDirectory);
                log.info("FTP ==> current directory: {}", con.printWorkingDirectory());
                boolean result = con.storeFile(pathOnFTPServer.getFileName().toString(), inputStream);
                log.info("boolean result|" + result);
                inputStream.close();
                con.logout();
                con.disconnect();
                if(result)
                    log.info("You successfully uploaded|serverDirectory|" + serverDirectory);
                else
                    log.info("You failed uploaded|serverDirectory|" + serverDirectory);
            }
        } catch (Exception e) {
            log.error("You failed uploaded|");
            log.error("ERROR|" + e.getMessage(), e);
        }
    }

    public Path createImageFileVHtm(String thumbUpload, String type, String serverDirectory) {
        try {
            if(thumbUpload == null || thumbUpload.equals(""))
                return null;
            String[] dataArray = thumbUpload.trim().split(",");
            String imgBase64 = "";
            String fileExtension = "jpg";
            if (dataArray.length > 1) {
                imgBase64 = dataArray[1];
                fileExtension = dataArray[0].replace("data:image/", "").replace(";base64", "");
            } else {
                imgBase64 = thumbUpload;
            }
            String fileName = Helper.generateRandomString(32);

            Path relativePath;
            if(serverDirectory.equals("music"))
                relativePath = Paths.get(appConfiguration.getFtpMusicDirectory()).resolve(type).resolve(Helper.getTimeNow());
            else
                relativePath = Paths.get(appConfiguration.getFtpAudioDirectory()).resolve(type).resolve(Helper.getTimeNow());

            relativePath = relativePath.resolve(fileName + "." + fileExtension);
            log.info("PATH|" + "|relativePath = " + relativePath);

            try {
                uploadBase64Img2FTP(imgBase64, relativePath);
            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
            }

            return relativePath;

        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }

    public Path saveFileChunk2Storage(MultipartFile fileChunk, String type){
        try {
            Path ROOT_FOLDER = Paths.get(appConfiguration.getRootPath());

            String originalFilename = fileChunk.getOriginalFilename();

            if(!StringUtils.hasLength(originalFilename))
                return null;

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

            if(!StringUtils.hasLength(fileExtension))
                return null;

            String fileName = Helper.generateRandomString(32);
            Path timePath = Helper.getPathByTime();

            Path relativePath = Paths.get(appConfiguration.getFileInDBPrefix()).resolve(type).resolve(timePath);

            relativePath = relativePath.resolve(fileName + "." + fileExtension);

            Path obsolutePath = ROOT_FOLDER.resolve(relativePath);

            log.info("CHUNK|" + fileChunk.getContentType() + "|relativePath = " + relativePath + "|obsolutePath = " + obsolutePath);
            Path folderPath = obsolutePath.getParent();
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            try (OutputStream os = Files.newOutputStream(obsolutePath)) {
                os.write(fileChunk.getBytes());
                os.flush();
            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
                return null;
            }

            return obsolutePath;
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }


    public void uploadFile2FTP(MultipartFile file, String[] timePath) {
        if(timePath == null)
            return;
        String FTP_ADDRESS = appConfiguration.getFTPAddress();
        String LOGIN = appConfiguration.getFTPLogin();
        String PSW = appConfiguration.getFTPPWS();
        String serverDirectory = timePath[0];
        Path pathOnFTPServer = Paths.get(serverDirectory);
        log.info("serverDirectory|" + serverDirectory);
        final FTPClient con = new FTPClient();

        try {
            con.connect(FTP_ADDRESS);
            if (con.login(LOGIN, PSW)) {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);
                pathOnFTPServer.forEach(p -> {
                    String folder = p.toString();
                    try {
                        if(!con.changeWorkingDirectory(folder)) {
                            con.makeDirectory(folder);
                            con.changeWorkingDirectory(folder);
                        }
                    } catch (Exception ex) {
                        log.error("ERROR---uploadFile2FTP|" + ex.getMessage(), ex);
                    }
                });
                log.info("Server directory|{}", timePath[0]);
                boolean result = con.storeFile(timePath[0], file.getInputStream());
                log.info("boolean result|" + result);
                con.logout();
                con.disconnect();
                if(result)
                    log.info("You successfully uploaded|" + file.getOriginalFilename() + "!");
                else
                    log.info("You failed uploaded|" + file.getOriginalFilename() + "!");
            }
        } catch (Exception e) {
            log.error("You failed uploaded|" + file.getOriginalFilename() + "!");
            log.error("ERROR|" + e.getMessage(), e);
        }
    }
    public Path createMusicImage(String imgBase64Data, String type) {
        try {
            if(imgBase64Data == null || imgBase64Data.equals(""))
                return null;
            String[] dataArray = imgBase64Data.trim().split(",");
            String imgBase64 = "";
            String fileExtension = "jpg";
            if (dataArray.length > 1) {
                imgBase64 = dataArray[1];
                fileExtension = dataArray[0].replace("data:image/", "").replace(";base64", "");
            } else {
                imgBase64 = imgBase64Data;
            }
            String fileName = Helper.generateRandomString(32);

            Path pathOnFTPServer = Paths.get(appConfiguration.getFtpMusicDirectory()).resolve(type).resolve(Helper.getTimeNow());
            pathOnFTPServer = pathOnFTPServer.resolve(fileName + "." + fileExtension);

            log.info("PATH|" + "|pathOnFTPServer = " + pathOnFTPServer);

            try {
                uploadBase64Img2FTP(imgBase64, pathOnFTPServer);

            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
            }


            return pathOnFTPServer;
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }

    public Path createMusicFile(MultipartFile audioFile, String type) {
        try {
            if (audioFile == null || audioFile.isEmpty())
                return null;

            // Lấy phần mở rộng từ tên tệp gốc
            String originalFileName = audioFile.getOriginalFilename();
            String fileExtension = "mp3"; // Đặt mặc định là mp3 hoặc phần mở rộng tệp âm thanh bạn mong đợi

            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
            }

            String fileName = Helper.generateRandomString(32);

//            String[] timePath = Helper.getTimeNowV2();

            Path pathOnFTPServer = Paths.get(appConfiguration.getFtpMusicDirectory()).resolve(type).resolve(Helper.getTimeNow());
            pathOnFTPServer = pathOnFTPServer.resolve(fileName + "." + fileExtension);

            log.info("PATH|" + "|pathOnFTPServer = " + pathOnFTPServer);

            try {
                uploadFTPAudioFile(audioFile, pathOnFTPServer);
            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
            }

            return pathOnFTPServer;
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }

    public Path createCrbtFile(MultipartFile audioFile, String type) {
        try {
            if (audioFile == null || audioFile.isEmpty())
                return null;

            // Lấy phần mở rộng từ tên tệp gốc
            String originalFileName = audioFile.getOriginalFilename();
            String fileExtension = "wav"; // Đặt mặc định là mp3 hoặc phần mở rộng tệp âm thanh bạn mong đợi

            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
            }

            String fileName = Helper.generateRandomString(32);

//            String[] timePath = Helper.getTimeNowV2();

            Path pathOnFTPServer = Paths.get(appConfiguration.getFtpMediaDirectoryCrbt()).resolve(type).resolve(Helper.getTimeNow());
            pathOnFTPServer = pathOnFTPServer.resolve(fileName + "." + fileExtension);

            log.info("PATH|" + "|pathOnFTPServer = " + pathOnFTPServer);

            try {
                uploadFTPAudioFileCrbt(audioFile, pathOnFTPServer);
            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
            }

            return pathOnFTPServer;
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }

    public Path createMediaImage(String imgBase64Data, String type) {
        try {
            if(imgBase64Data == null || imgBase64Data.equals(""))
                return null;
            String[] dataArray = imgBase64Data.trim().split(",");
            String imgBase64 = "";
            String fileExtension = "jpg";
            if (dataArray.length > 1) {
                imgBase64 = dataArray[1];
                fileExtension = dataArray[0].replace("data:image/", "").replace(";base64", "");
            } else {
                imgBase64 = imgBase64Data;
            }
            String fileName = Helper.generateRandomString(32);
//            String[] timePath = Helper.getTimeNowV2();

            Path pathOnFTPServer = Paths.get(appConfiguration.getFtpMediaDirectory()).resolve(type).resolve(Helper.getTimeNow());
            pathOnFTPServer = pathOnFTPServer.resolve(fileName + "." + fileExtension);

            log.info("PATH|" + "|pathOnFTPServer = " + pathOnFTPServer);

            try {
                uploadBase64Img2FTP(imgBase64, pathOnFTPServer);

            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
            }

            return pathOnFTPServer;
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }
    public void uploadFTPAudioFile(MultipartFile file, Path pathOnFTPServer) {
        log.info("relativePath|" + pathOnFTPServer.toString());
        String serverDirectory = pathOnFTPServer.toString();

        String FTP_ADDRESS = appConfiguration.getFTPAddress();
        String LOGIN = appConfiguration.getFTPLogin();
        String PSW = appConfiguration.getFTPPWS();
        final FTPClient con = new FTPClient();
        InputStream inputStream = null;

        try {
            con.connect(FTP_ADDRESS);
            if (con.login(LOGIN, PSW)) {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);
                log.info("FTP ==> current directory: {}", con.printWorkingDirectory());
                Path parentPath = pathOnFTPServer.getParent();
                log.info("FTP ==> parentPath: {}", parentPath.toString());

                parentPath.forEach(p -> {
                    String folder = p.toString();
                    try {
                        if(!con.changeWorkingDirectory(folder)) {
                            con.makeDirectory(folder);
                            con.changeWorkingDirectory(folder);
                        }
                    } catch (Exception ex) {
                        log.error("Cannot create folder : {}", folder);
                    }
                });
                inputStream = file.getInputStream();
                log.info("Server directory|{}", serverDirectory);
                log.info("FTP ==> current directory: {}", con.printWorkingDirectory());
                boolean result = con.storeFile(pathOnFTPServer.getFileName().toString(), inputStream);
                log.info("boolean result|" + result);
                inputStream.close();
                con.logout();
                con.disconnect();
                if(result)
                    log.info("You successfully uploaded|serverDirectory|" + serverDirectory);
                else
                    log.info("You failed uploaded|serverDirectory|" + serverDirectory);
            }
        } catch (Exception e) {
            log.error("You failed uploaded|");
            log.error("ERROR|" + e.getMessage(), e);
        }
    }

    public void uploadFTPAudioFileCrbt(MultipartFile file, Path pathOnFTPServer) {
        log.info("relativePath|" + pathOnFTPServer.toString());
        String serverDirectory = pathOnFTPServer.toString();

        String FTP_ADDRESS = appConfiguration.getFTPAddressCrbt();
        String LOGIN = appConfiguration.getFTPLoginCrbt();
        String PSW = appConfiguration.getFTPPwsCrbt();
        final FTPClient con = new FTPClient();
        InputStream inputStream = null;

        try {
            con.connect(FTP_ADDRESS);
            if (con.login(LOGIN, PSW)) {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                // Chuyển thư mục sau khi đăng nhập thành công
                String targetDirectory = "/u02/umusic";
                boolean cdResult = con.changeWorkingDirectory(targetDirectory);
                if (cdResult) {
                    log.info("Successfully changed directory to: {}", targetDirectory);
                } else {
                    log.error("Failed to change directory to: {}", targetDirectory);
                }

                log.info("FTP ==> current directory: {}", con.printWorkingDirectory());
                Path parentPath = pathOnFTPServer.getParent();
                log.info("FTP ==> parentPath: {}", parentPath.toString());

                parentPath.forEach(p -> {
                    String folder = p.toString();
                    try {
                        if (!con.changeWorkingDirectory(folder)) {
                            con.makeDirectory(folder);
                            con.changeWorkingDirectory(folder);
                        }
                    } catch (Exception ex) {
                        log.error("Cannot create folder : {}", folder);
                    }
                });

                inputStream = file.getInputStream();
                log.info("Server directory|{}", serverDirectory);
                log.info("FTP ==> current directory: {}", con.printWorkingDirectory());

                // Upload the file
                boolean result = con.storeFile(pathOnFTPServer.getFileName().toString(), inputStream);
                log.info("boolean result|" + result);

                inputStream.close();

                if (result) {
                    String needChmod = serverDirectory;
                    log.info("Path need split|" + needChmod);
                    if (needChmod.startsWith("/")) {
                        needChmod = needChmod.substring(1);
                    }
                    if (needChmod.endsWith("/")) {
                        needChmod = needChmod.substring(0, needChmod.length() - 1);
                    }
                    String[] arrChmod = needChmod.split("/");
                    log.info("arr: " + String.join(",", arrChmod));

                    String currentPath = con.printWorkingDirectory(); // Lưu trữ đường dẫn hiện tại

                    for (int i = arrChmod.length - 1; i >= 0; i--) {
                        // Set permissions to 777 after file is uploaded
                        String command = "chmod 777 " + arrChmod[i];
                        boolean chmodSuccess = con.sendSiteCommand(command);
                        log.info("CHMOD result|" + chmodSuccess);

                        if (chmodSuccess) {
                            log.info(command + "|File permissions changed to 777 successfully.");
                        } else {
                            log.info(command + "|Failed to change file permissions.");
                        }

                        // Tạo đường dẫn đến thư mục cha
                        String parentDirectory = currentPath.substring(0, currentPath.lastIndexOf('/'));

                        // Thay đổi thư mục đến thư mục cha
                        boolean backResult = con.changeWorkingDirectory(parentDirectory);
                        if (backResult) {
                            currentPath = parentDirectory; // Cập nhật đường dẫn hiện tại
                            log.info("Successfully changed directory to: {}", currentPath);
                        } else {
                            log.error("Failed to change directory to: {}", parentDirectory);
                        }
                    }
                }

                con.logout();
                con.disconnect();

                if (result) {
                    log.info("You successfully uploaded|serverDirectory|" + serverDirectory);
                } else {
                    log.info("You failed uploaded|serverDirectory|" + serverDirectory);
                }
            }
        } catch (Exception e) {
            log.error("You failed uploaded|");
            log.error("ERROR|" + e.getMessage(), e);
        }
    }

    public Path createMediaFile(MultipartFile file, String type) {
        try {
            if (file == null || file.isEmpty())
                return null;

            // Lấy phần mở rộng từ tên tệp gốc
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "vtt";

            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
            }

            String fileName = Helper.generateRandomString(32);


            Path pathOnFTPServer = Paths.get(appConfiguration.getFtpMediaDirectory()).resolve(type).resolve(Helper.getTimeNow());
            pathOnFTPServer = pathOnFTPServer.resolve(fileName + "." + fileExtension);

            log.info("PATH|" + "|pathOnFTPServer = " + pathOnFTPServer);

            try {
                //upload file vtt dùng lại hàm audio
                uploadFTPAudioFile(file, pathOnFTPServer);
            } catch (Exception e) {
                log.error("ERROR|" + e.getMessage(), e);
            }


            return pathOnFTPServer;
        } catch (Exception e) {
            log.error("ERROR|" + e.getMessage(), e);
        }
        return null;
    }
}