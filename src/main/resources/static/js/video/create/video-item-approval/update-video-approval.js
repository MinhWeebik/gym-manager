document.addEventListener("DOMContentLoaded", function () {
    const uploadForm = document.getElementById("form-update-video-item-approval");
    const id = document.getElementById("id");
    const statusForm = document.getElementById("statusForm");
    const titleInput = document.getElementById("videoTitle");
    const cateIdInput = document.getElementById("cateId");
    const channelIdInput = document.getElementById("channelId");
    const activedInput = document.getElementById("actived");
    const publishTimeInput = document.getElementById("publishTime");
    const descriptionInput = document.getElementById("videoDesc");
    const isNew = document.getElementById("isNew");
    const isHot = document.getElementById("isHot");
    const fileAvatar = document.getElementById("videoImage");
    const fileInput = document.getElementById("videoMedia");
    const submitButton = document.getElementById("updateButton");

    const progressPopup = document.getElementById("progressPopup");
    const progressBar = document.getElementById("progressBar");
    const progressText = document.getElementById("progressText");
    const sucsessPopup = document.getElementById("sucsessPopup");

    const videoSrc = document.getElementById("here");

    let fileChunks = [];
    let currentChunkIndex = 0;
    let uploadId = null;
    let videoPath = "";

    uploadForm.addEventListener("submit", function (event) {
        console.log("CLICK")
        event.preventDefault(); // Prevent default form submission

        const file = fileInput ? fileInput.files[0] : null;

        if (!file || !videoPath || !fileAvatar.value) {
            // None of the file inputs have data, proceed to the final API call
            uploadFile(videoSrc.src); // Pass null as videoPath since no video to upload
            return;
        } else {
            const formData = new FormData();
            formData.append("fileName", file.name);
            formData.append("fileSize", file.size);
            formData.append("extension", getFileExtension(file.name));

            openPopup();

            // Step 1: Request uploadId from the /upload API using XMLHttpRequest
            const xhrUpload = new XMLHttpRequest();
            xhrUpload.open("POST", "upload", true);
            xhrUpload.onreadystatechange = function () {
                if (xhrUpload.readyState === XMLHttpRequest.DONE) {
                    if (xhrUpload.status === 200) {
                        uploadId = xhrUpload.responseText;

                        // Start the video upload process
                        prepareFileChunks(file);
                        uploadChunks(formData, file);
                    } else {
                        closePopup();
                        console.error("Upload initiation error:", xhrUpload.status);
                        alert("Upload initiation failed.");
                    }
                }
            };
            xhrUpload.send(formData);
        }
    });

    function getFileExtension(filename) {
        const parts = filename.split('.');
        if (parts.length > 1) {
            return parts.pop();
        }
        return "";
    }

    function openPopup() {
        progressPopup.style.display = "block";
        submitButton.style.display = "none";
    }

    function closePopup() {
        progressPopup.style.display = "none";
        sucsessPopup.style.display = "block";
    }

    function prepareFileChunks(file) {
        const chunkSize = 100 * 1024 * 1024; // 100MB chunks (change as needed)
        let offset = 0;

        while (offset < file.size) {
            fileChunks.push(file.slice(offset, offset + chunkSize));
            offset += chunkSize;
        }
    }

    function uploadChunks(formData, file) {
        if (currentChunkIndex >= fileChunks.length) {
            // All chunks uploaded
            closePopup();
            return;
        }

        const chunk = fileChunks[currentChunkIndex];

        if (!chunk) {
            closePopup();
            console.error("Chunk is undefined at index:", currentChunkIndex);
            alert("Chunk is undefined.");
            return;
        }

        const chunkFormData = new FormData();
        chunkFormData.append("uploadId", uploadId);
        chunkFormData.append("chunkIndex", currentChunkIndex); // Add chunk index here
        chunkFormData.append("chunkSize", chunk.size);
        chunkFormData.append("fileSize", file.size);
        chunkFormData.append("fileChunkUpload", chunk);

        // Step 2: Upload chunk to the /upload-chunk API using XMLHttpRequest
        const xhrUploadChunk = new XMLHttpRequest();
        xhrUploadChunk.open("POST", "upload-chunk", true);
        xhrUploadChunk.onreadystatechange = function () {
            if (xhrUploadChunk.readyState === XMLHttpRequest.DONE) {
                if (xhrUploadChunk.status === 200) {
                    console.log(xhrUploadChunk.response)
                    var result = JSON.parse(xhrUploadChunk.response);

                    if (result.status === '0') {
                        console.log(result.value);
                        currentChunkIndex++;
                        const progress = (currentChunkIndex / fileChunks.length) * 100;
                        progressBar.value = progress;
                        progressText.textContent = `${progress.toFixed(2)}%`;

                        // Upload the next chunk
                        uploadChunks(formData, file);
                    }else if (result.status === '1') {
                        // Assume result is a path
                        console.log(result.value);
                        videoPath = result.value; // Assign the video path to the input
                        uploadFile(videoPath); // Continue uploading the next chunks
                    } else {
                        closePopup();
                        console.error("Chunk upload error:", xhrUploadChunk.status);
                        alert("Chunk upload failed.");
                    }
                }
            }
        };
        xhrUploadChunk.send(chunkFormData);
    }

    // Step 3: Send formData to /save-video-feature using XMLHttpRequest
    function uploadFile(videoPath) {
        console.log(videoPath);
        console.log(titleInput.value);
        console.log(cateIdInput.value);
        console.log(channelIdInput.value);
        console.log(publishTimeInput.value);
        console.log(descriptionInput.value);
        console.log(isNew.value);
        console.log(isHot.value);
        console.log(fileAvatar.value);

        const formData = new FormData();
        formData.append("videoMedia", videoPath);
        formData.append("statusForm", statusForm.value);
        formData.append("id", id.value);
        formData.append("videoTitle", titleInput.value);
        formData.append("cateId", cateIdInput.value);
        formData.append("channelId", channelIdInput.value);
        formData.append("actived", activedInput.value);
        formData.append("publishTime", publishTimeInput.value);
        formData.append("videoDesc", descriptionInput.value);
        formData.append("isNew", isNew.value);
        formData.append("isHot", isHot.value);
        formData.append("videoImage", fileAvatar.value);

        console.log(formData);

        const xhr = new XMLHttpRequest();
        xhr.open("POST","/" + window.location.pathname.split('/')[1] + "/video/video-item/save", true);
        xhr.onreadystatechange = function () {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    closePopup();
                    const redirectUrl  = xhr.responseText;
                    if (redirectUrl) {
                        window.location.href = '/' + window.location.pathname.split('/')[1] + redirectUrl; // Thực hiện chuyển hướng tới trang web
                        console.log("URL: " + window.location.href)
                    } else {
                        alert("Upload successful!");
                    }
                } else {
                    closePopup();
                    alert("Upload failed.");
                }
            }
        };

        console.log(formData);
        xhr.send(formData);
    }
});
