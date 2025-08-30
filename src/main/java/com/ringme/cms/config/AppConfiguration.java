package com.ringme.cms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Service
@Getter
@Configuration
public class AppConfiguration {

    @Value("${cms.file.in.db.prefix}")
    private String fileInDBPrefix;

    @Value("${cms.file.store.root-path}")
    private String rootPath;

    @Value("${ftp-address}")
    private String FTPAddress;

    @Value("${ftp-login}")
    private String FTPLogin;

    @Value("${ftp-psw}")
    private String FTPPWS;

    @Value("${api-convert-video}")
    private String apiConvertVideo;

    @Value("${api-convert-cdn}")
    private String apiConvertCdn;

    @Value("${server-directory-image}")
    private String ftpImageDirectory;

    @Value("${server-directory-audio}")
    private String ftpAudioDirectory;

    @Value("${server-directory-music}")
    private String ftpMusicDirectory;

    @Value("${server-directory-media}")
    private String ftpMediaDirectory;

    @Value("${ftp-address-crbt}")
    private String fTPAddressCrbt;

    @Value("${ftp-login-crbt}")
    private String fTPLoginCrbt;

    @Value("${ftp-psw-crbt}")
    private String fTPPwsCrbt;

    @Value("${server-directory-media-crbt}")
    private String ftpMediaDirectoryCrbt;

    @Value("${domain-api-umusic}")
    private String domainApiUmusic;

    @Value("${laoid-clientId}")
    private String laoidClientId;

    @Value("${laoid-callbackUri}")
    private String laoidCallbackUri;

    @Value("${laoid-clientSecret}")
    private String laoidClientSecret;

    @Value("${laoid-id}")
    private String laoidID;

    @Value("${excel-file-path-crbt}")
    private String excelFilePathCrbt;

    @Value("${max-row-read-crbt}")
    private Integer maxRowReadCrbt;

    @Value("${src-clawer-crbt}")
    private String srcClawerCrbt;

    @Value("${split-url-clawer-crbt}")
    private String splitUrlClawerCrbt;

    @Value("${ffmpeg-path}")
    private String ffmpegPath;
}
