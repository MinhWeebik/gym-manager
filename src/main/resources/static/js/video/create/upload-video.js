document.addEventListener("DOMContentLoaded", function () {
    const uploadForm = document.getElementById("form");
//    const titleInput = document.getElementById("title");
//    const descriptionInput = document.getElementById("description");
//    const enableChat = document.getElementById("enable_chat");
//    const privacy = document.getElementById("privacy");
    const mediaTitle = document.getElementById("mediaTitle");
    const id = document.getElementById("id");
    const cateId = document.getElementById("cateId");
    const categoryTypeId = document.getElementById("categoryTypeId");
    const listProducer = document.getElementById("listProducer");
    const listActor = document.getElementById("listActor");
    const listDirector = document.getElementById("listDirector");
    const serviceId = document.getElementById("serviceId");
    const isEpisode = document.getElementById("isEpisode");
    const productionTime = document.getElementById("productionTime");
    const isCopyright = document.getElementById("isCopyright");
    const copyrightId = document.getElementById("copyrightId");
    const mediaDesc = document.getElementById("mediaDesc");
    const vtt = document.getElementById("vtt");
    const publishTime = document.getElementById("publishTime");
    const actived = document.getElementById("actived");

        // Tạo một mảng để lưu trữ các giá trị đã chọn
        var profileFilm = [];
            // Tạo một mảng để lưu trữ các giá trị đã chọn
            var profileAudio = [];

    const fileAvatar = document.getElementById("file1");
    const fileInput = document.getElementById("videoUpload");
    const submitButton = document.getElementById("submitButton");
    const progressPopup = document.getElementById("progressPopup");
    const progressBar = document.getElementById("progressBar");
    const progressText = document.getElementById("progressText");
    const sucsessPopup = document.getElementById("sucsessPopup");

    let fileChunks = [];
    let currentChunkIndex = 0;
    let uploadId = null;
    let videoPath = "";

    uploadForm.addEventListener("submit", function (event) {

        var divElement = document.getElementById("div-content-checkbox-1");
        var checkboxes = divElement.querySelectorAll('input[name="profile-film[]"]');
        checkboxes.forEach(function(checkbox) {
            if (checkbox.checked) {
                profileFilm.push(checkbox.value);
            }
        });
        console.log(profileFilm);

        var divElement = document.getElementById("div-content-checkbox-2");
        var checkboxes2 = divElement.querySelectorAll('input[name="profile-audio[]"]');
        checkboxes2.forEach(function(checkbox) {
            if (checkbox.checked) {
                profileAudio.push(checkbox.value);
            }
        });
        console.log(profileAudio);


        event.preventDefault(); // Prevent default form submission

        const file = fileInput.files[0];

        if (atView == "media-approve") {
            mediaApprove();
        }

        if (!file) {
            uploadFile(null);
        }

        const formData = new FormData();
        formData.append("fileName", file.name);
        formData.append("fileSize", file.size);
        formData.append("extension", getFileExtension(file.name));

        openPopup();

        // Step 1: Request uploadId from the /upload API using XMLHttpRequest
        const xhrUpload = new XMLHttpRequest();
        xhrUpload.open("POST", "/" + window.location.pathname.split('/')[1] + "/cdn/media/upload", true);
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
    });

    function getFileExtension(filename) {
        // return filename.split('.').pop();
        const parts = filename.split('.');
        if (parts.length > 1) {
            return parts.pop();
        }
        return ""; // Trả về chuỗi rỗng nếu không tìm thấy phần mở rộng
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
        xhrUploadChunk.open("POST", "/" + window.location.pathname.split('/')[1] + "/cdn/media/upload-chunk", true);
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

        const formData = new FormData();
        formData.append("mediaPath", videoPath);
        formData.append("mediaTitle", mediaTitle.value);
        formData.append("id", id.value);
        formData.append("cateId", cateId.value);
        formData.append("categoryTypeId", categoryTypeId.value);
        formData.append("listProducer", Array.from(listProducer.selectedOptions).map(option => option.value));
        formData.append("listActor", Array.from(listActor.selectedOptions).map(option => option.value));
        formData.append("listDirector", Array.from(listDirector.selectedOptions).map(option => option.value));
        formData.append("isEpisode", isEpisode.value);
        formData.append("productionTime", productionTime.value);
        formData.append("isCopyright", isCopyright.value);
        formData.append("copyrightId", copyrightId.value);
        formData.append("vtt", vtt.files[0]);
        formData.append("mediaDesc", mediaDesc.value);
        formData.append("profile-film[]", profileFilm);
        formData.append("profile-audio[]", profileAudio);
        formData.append("thumbUpload", fileAvatar.value);

        const xhr = new XMLHttpRequest();
        xhr.open("POST", "/" + window.location.pathname.split('/')[1] + "/cdn/media/save", true);
        xhr.onreadystatechange = function () {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    closePopup();
                    const redirectUrl  = xhr.responseText;
                    if (redirectUrl) {
                        if(cateId.value == 1)
                            window.location.href = '/' + window.location.pathname.split('/')[1] + '/cdn/media/list-movie'; // Thực hiện chuyển hướng tới trang web
                        else if (cateId.value == 2)
                            window.location.href = '/' + window.location.pathname.split('/')[1] + '/cdn/media/list-audio'; // Thực hiện chuyển hướng tới trang web
                        else if (cateId.value == 3)
                            window.location.href = '/' + window.location.pathname.split('/')[1] + '/cdn/media/list-video'; // Thực hiện chuyển hướng tới trang web
                        else
                            window.location.href = '/' + window.location.pathname.split('/')[1] + '/cdn/media/index'; // Thực hiện chuyển hướng tới trang web
                    } else {
                        alert("Upload successful!");
                    }
                } else {
                    closePopup();
                    alert("Upload failed.");
                }
            }
        };

        xhr.send(formData);
    }

    function mediaApprove() {
            const formData = new FormData();
            formData.append("id", id.value);
            formData.append("serviceId", Array.from(serviceId.selectedOptions).map(option => option.value));
            formData.append("actived", actived.value);
            formData.append("publishTime", publishTime.value);

            const xhr = new XMLHttpRequest();
            xhr.open("POST", "/" + window.location.pathname.split('/')[1] + "/cdn/media/save-approve", true);
            xhr.onreadystatechange = function () {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    if (xhr.status === 200) {
                        closePopup();
                        const redirectUrl  = xhr.responseText;
                        if (redirectUrl) {
                            if(cateId.value == 1)
                                window.location.href = '/' + window.location.pathname.split('/')[1] + '/cdn/media/list-movie'; // Thực hiện chuyển hướng tới trang web
                            else if (cateId.value == 2)
                                window.location.href = '/' + window.location.pathname.split('/')[1] + '/cdn/media/list-audio'; // Thực hiện chuyển hướng tới trang web
                            else if (cateId.value == 3)
                                window.location.href = '/' + window.location.pathname.split('/')[1] + '/cdn/media/list-video'; // Thực hiện chuyển hướng tới trang web
                            else
                                window.location.href = '/' + window.location.pathname.split('/')[1] + '/cdn/media/index'; // Thực hiện chuyển hướng tới trang web
                        } else {
                            alert("Upload successful!");
                        }
                    } else {
                        closePopup();
                        alert("Upload failed.");
                    }
                }
            };

            xhr.send(formData);
        }

const videoUpload = document.getElementById("videoUpload");
        const videoSrcInput = document.getElementById("video_src");
//        const videoPreview = document.querySelector(".image-preview video");
//        const editVideoButton = document.getElementById("icon-button-edit-video");
//        const deleteVideoButton = document.getElementById("icon-button-delete-video");
        const source = document.getElementById("here");
        const chooseVid = document.getElementById("chooseVid");


        function showVideoButtons() {
//            editVideoButton.style.display = "inline-block";
//            deleteVideoButton.style.display = "inline-block";
        }

        function hideVideoButtons() {
//            editVideoButton.style.display = "none";
//            deleteVideoButton.style.display = "none";
        }

        // Kiểm tra khi tải trang xem có video hay không
        if (videoSrcInput.value) {
            showVideoButtons();
//            videoPreview.style.display = "block";
        } else {
            hideVideoButtons();
//            videoPreview.style.display = "none";
        }

        // Sự kiện xóa video
//        deleteVideoButton.addEventListener("click", function () {
//            videoSrcInput.value = ""; // Xóa giá trị video_src
//            videoPreview.src = ""; // Xóa nguồn video
//            source.src = "";
//            hideVideoButtons();
//            videoPreview.style.display = "none";
//            chooseVid.style.display = "block";
//            // Có thể thêm mã để gửi request lên server để xóa video
//        });

        // Sự kiện edit để chọn video khác
//        editVideoButton.addEventListener("click", function () {
//            videoUpload.click(); // Kích hoạt sự kiện click trên input type="file"
//        });

        // Xử lý sự kiện khi chọn video mới
        videoUpload.addEventListener("change", function () {
            const selectedVideo = videoUpload.files[0];
            if (selectedVideo) {
                // Cập nhật giá trị video_src
                videoSrcInput.value = selectedVideo.name;
                // Cập nhật nguồn video
//                videoPreview.src = URL.createObjectURL(selectedVideo);
                showVideoButtons();
//                videoPreview.style.display = "block";
                // Có thể thêm mã để gửi request lên server để lưu video mới
            }
        });
});
