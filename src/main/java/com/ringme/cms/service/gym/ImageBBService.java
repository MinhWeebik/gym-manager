package com.ringme.cms.service.gym;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringme.cms.common.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageBBService {
    @Value("${imgbb_api_key}")
    private String API_KEY;
    private final String UPLOAD_URL = "https://api.imgbb.com/1/upload";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    public String uploadImage(String thumbUpload) throws IOException {
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
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("key", API_KEY);
        body.add("image", imgBase64);
        body.add("name", fileName);
        String response = restTemplate.postForObject(UPLOAD_URL, body, String.class);
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("url").asText();
    }
}
