let fileInput2 = document.getElementById("file2");
let fileInput3 = document.getElementById("videoImage");
let image2 = document.getElementById("image2");
let aspectRatio = document.querySelectorAll(".aspect-ratio-button");
const previewImg2 = document.getElementById("previewImage2");
const applyButton2 = document.getElementById("apply2");
let cropper2 = "";
let fileName2 = "";
const closePopupButton2 = document.getElementById("closePopupButton2");
const popupContainer2 = document.getElementById("popupContainer2");
const dnd2Element = document.querySelector('#dnd2');

dnd2Element.addEventListener('dragover', function(e) {
    e.preventDefault()
    this.classList.add('drag-over')
})

dnd2Element.addEventListener('dragleave', function(e) {
    e.preventDefault()
    this.classList.remove('drag-over')
})

dnd2Element.addEventListener('drop', function(e) {
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
        image2.setAttribute("src", reader.result);
        if (cropper2) {
            cropper2.destroy();
        }
        cropper2 = new Cropper(image2, {
            aspectRatio: 16 / 9,
        });

        // Thêm điều kiện để chỉ mở popup khi đã tải lên ảnh
        if (file.size > 0) {
            popupContainer2.style.display = "block";
        }
    };
    fileName2 = file.name.split(".")[0];
}


closePopupButton2.addEventListener("click", () => {
    popupContainer2.style.display = "none";
});

fileInput2.onchange = () => {
    let reader = new FileReader();
    console.log(reader)
    reader.readAsDataURL(fileInput2.files[0]);

    reader.onload = () => {
        image2.setAttribute("src", reader.result);
        if (cropper2) {
            cropper2.destroy();
        }
        cropper2 = new Cropper(image2, {
            aspectRatio: 16 / 9,
        });

        // Thêm điều kiện để chỉ mở popup khi đã tải lên ảnh
        if (fileInput2.files.length > 0) {
            popupContainer2.style.display = "block";
        }
    };
    fileName2 = fileInput2.files[0].name.split(".")[0];
};

//Set aspect ration
aspectRatio.forEach((element) => {
    element.addEventListener("click", () => {
        if (element.innerText == "Free") {
            cropper2.setAspectRatio(NaN);
        } else {
            cropper2.setAspectRatio(eval(element.innerText.replace(":", "/")));
        }
    });
});

applyButton2.addEventListener("click", (e) => {
    e.preventDefault();
    let imgSrc = cropper2.getCroppedCanvas({}).toDataURL("image/jpeg", 0.75);
    //Set previewImgOutPopup
    previewImg2.src = imgSrc;
    fileInput3.value = imgSrc;

    $('img[id="previewImage2"]').closest('img[id="previewImage2"]').show();
    $('h4[id="chooseImg2"]').closest("h4").hide();
    document.getElementById("popupContainer2").style.display = "none";
});

window.onload = () => {};