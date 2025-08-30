let fileInput = document.getElementById("file");
let fileInput1 = document.getElementById("avatar");
let image = document.getElementById("image");
let aspectRatio = document.querySelectorAll(".aspect-ratio-button");
const previewImg = document.getElementById("previewImage");
const applyButton = document.getElementById("apply");
const options = document.querySelector(".options");
let cropper = "";
let fileName = "";
const closePopupButton = document.getElementById("closePopupButton");
const popupContainer = document.getElementById("popupContainer");
const dndElement = document.querySelector('#dnd');
const validImageTypes = ['image/gif', 'image/jpeg', 'image/png'];

dndElement.addEventListener('dragover', function(e) {
    e.preventDefault()
    this.classList.add('drag-over')
})

dndElement.addEventListener('dragleave', function(e) {
    e.preventDefault()
    this.classList.remove('drag-over')
})

dndElement.addEventListener('drop', function(e) {
    e.preventDefault()
    const files = e.dataTransfer.files;
    for (var i = 0; i < files.length; i++) {
        const file = files[i]
        renderPreviewImage(file)
    }
})

function renderPreviewImage(file) {
    const fileType = file['type']

    if (!validImageTypes.includes(fileType)) {
        resultElement.insertAdjacentHTML(
            'beforeend',
            '<span class="preview-img">Chọn ảnh đi :3</span>'
        )
        return
    }

    let  reader = new FileReader();
    reader.readAsDataURL(file);

    reader.onload = () => {
        const originalImage = new Image();
        originalImage.src = reader.result;

        originalImage.onload = () => {
            if (originalImage.width < 200 || originalImage.height < 200) {
                // Kiểm tra kích thước ảnh ban đầu
                alert('Kích thước ảnh quá nhỏ. Chọn ảnh có kích thước ít nhất 100x100 px.');
                return;
            }

            image.setAttribute("src", reader.result);
            if (cropper) {
                cropper.destroy();
            }
            cropper = new Cropper(image, {
                aspectRatio: 1 / 1, // Tỉ lệ khung hình 1:1
            //     viewMode: 1, // Đảm bảo kích thước cắt luôn là 200x200 px
                canvasWidth: 100, // Kích thước canvas ngang
                canvasHeight: 100, // Kích thước canvas dọc
            });

            // Thêm điều kiện để chỉ mở popup khi đã tải lên ảnh
            if (file.size > 0) {
                popupContainer.style.display = "block";
            }
        };
    };
    fileName = file.name.split(".")[0];
}


closePopupButton.addEventListener("click", () => {
    popupContainer.style.display = "none";
});

fileInput.onchange = () => {
    let reader = new FileReader();
    console.log(reader)
    reader.readAsDataURL(fileInput.files[0]);

    reader.onload = () => {
        image.setAttribute("src", reader.result);
        if (cropper) {
            cropper.destroy();
        }
        cropper = new Cropper(image, {
            aspectRatio: 1 / 1,
        });

        // Thêm điều kiện để chỉ mở popup khi đã tải lên ảnh
        if (fileInput.files.length > 0) {
            popupContainer.style.display = "block";
        }
    };
    fileName = fileInput.files[0].name.split(".")[0];
};

//Set aspect ration
aspectRatio.forEach((element) => {
    element.addEventListener("click", () => {
        if (element.innerText == "Free") {
            cropper.setAspectRatio(NaN);
        } else {
            cropper.setAspectRatio(eval(element.innerText.replace(":", "/")));
        }
    });
});

applyButton.addEventListener("click", (e) => {
    e.preventDefault();
    let imgSrc;
    const croppedCanvas = cropper.getCroppedCanvas({
        width: 100,
        height: 100,
    });

    if (croppedCanvas) {
        imgSrc = croppedCanvas.toDataURL("image/jpeg", 0.75);
    }
    previewImg.src = imgSrc;
    fileInput1.value = imgSrc;

    $('img[id="previewImage"]').closest('img[id="previewImage"]').show();
    $('h4[id="chooseImg"]').closest("h4").hide();
    document.getElementById("popupContainer").style.display = "none";
});

window.onload = () => {};