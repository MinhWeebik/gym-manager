package com.ringme.cms.utils;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.apache.tika.Tika;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;

public class FileUtils {
    private static final long MAX_SIZE_IN_BYTES = 500 * 1024; // 500KB
    private static final long MIN_DURATION_MS = 40000; // 40s
    private static final long MAX_DURATION_MS = 60000; // 60s

    public static String validateAudio(MultipartFile file) throws IOException {
        // Check MIME type
        Tika tika = new Tika();
        String mimeType = tika.detect(file.getInputStream());
        if (!mimeType.equals("audio/vnd.wave")) {
            return "The file is not in WAV format.";
        }

        // Check file size
        if (file.getSize() > MAX_SIZE_IN_BYTES) {
            return "The file size exceeds 500KB.";
        }

        // Validate audio properties
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(file.getBytes()))) {
            AudioFormat format = audioInputStream.getFormat();

            if (format.getChannels() != 1) {
                return "The file must have 1 channel (mono).";
            }

            if (format.getSampleSizeInBits() != 8) {
                return "The audio sample size must be 8-bit.";
            }

            if (format.getSampleRate() != 8000) {
                return "The audio sample rate must be 8000Hz.";
            }

            if (!format.getEncoding().toString().equals("ALAW")) {
                return "The audio format must be CCITT A-Law.";
            }

            long durationInMillis = (audioInputStream.getFrameLength() * 1000) / (int) format.getFrameRate();
            if (durationInMillis < MIN_DURATION_MS || durationInMillis > MAX_DURATION_MS) {
                return "The file duration must be between 40 and 60 seconds.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing the audio file.";
        }

        return "success";
    }

    public static long getWavDuration(File file) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
        long frameLength = fileFormat.getFrameLength();
        float frameRate = fileFormat.getFormat().getFrameRate();
        return (long) (frameLength / frameRate); // Thời gian tính bằng giây
    }

    public static Integer getBitRateFromMp3(File file)
            throws IOException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException {

        // Đọc file MP3
        AudioFile audioFile = AudioFileIO.read(file);
        AudioHeader audioHeader = audioFile.getAudioHeader();

        // Lấy bit rate
        return (int) audioHeader.getBitRateAsNumber();
    }

    public static long getMp3Duration(File file) throws IOException, UnsupportedAudioFileException {
        MpegAudioFileReader mp3Reader = new MpegAudioFileReader();
        AudioFileFormat fileFormat = mp3Reader.getAudioFileFormat(file);
        long frameLength = fileFormat.getFrameLength();
        float frameRate = fileFormat.getFormat().getFrameRate();
        return (long) (frameLength / frameRate); // Thời gian tính bằng giây
    }

    public static File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile); // Lưu file vào thư mục tạm thời
        return convFile;
    }

    public static MultipartFile convertFileToMultipartFile(File file) throws IOException {
        String name = file.getName();
        String originalFilename = file.getName();
        String contentType = Files.probeContentType(file.toPath());
        byte[] content = Files.readAllBytes(file.toPath());

        return new CustomMultipartFile(name, originalFilename, contentType, content);
    }

    public static File downloadFileFromUrl(String urlStr, String outputFileName) throws IOException {
        URL url = new URL(urlStr);
        File mp3File = new File(outputFileName);

        // Kiểm tra và tạo thư mục nếu chưa tồn tại
        File parentDir = mp3File.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(mp3File)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return mp3File;
    }

    public static void convertMP3ToWAVForCrbt(File mp3File, String wavFileName, String ffmpegPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath, "-y", "-i", mp3File.getAbsolutePath(),
                "-ar", "8000",               // Sample rate: 8 kHz
                "-ac", "1",                  // Channels: Mono
                "-ab", "64k",                // Bit rate: 64 kbps
                "-sample_fmt", "s16",        // Audio sample size: 16-bit
                "-acodec", "pcm_alaw",       // Audio format: CCITT A-Law
                wavFileName
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to convert MP3 to WAV. Exit code: " + exitCode);
        } else {
            System.out.println("Conversion successful.");
        }
    }
}
