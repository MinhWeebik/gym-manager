document.getElementById('approved').addEventListener('click', function() {
        addActived("12");
        document.getElementById('form').submit();
});

document.getElementById('disapproved').addEventListener('click', function() {
        addActived("8");
        document.getElementById('form').submit();
});

document.getElementById('deleteApprove').addEventListener('click', function() {
    let popup = document.getElementById('popup');
    let confirmButton = document.getElementById('confirmButton');
    let close = document.getElementById('close');
    // Hiển thị popup khi click vào biểu tượng
    popup.style.display = 'block';
    // Ẩn popup khi click vào nút xác nhận
        confirmButton.addEventListener('click', () => {
        popup.style.display = 'none';
        window.location.href = path + "/cdn/media/delete?id=" + document.getElementById("id").value;
    });
    // Ẩn popup khi click vào nút xác nhận
    close.addEventListener('click', () => {
        popup.style.display = 'none';
    });
});

$(document).on('click', function (event) {
    let $box = $('#myPopupApproval');
    // Kiểm tra nếu click không nằm trong $box và không nằm trong #approve
    if (!$box.has(event.target).length && !$(event.target).is('#approve') && !$(event.target).is('#close')) {
        removeSelectElement();
        $box.hide();
    }
});

function openPopupApproval(id) {
    $.ajax({
      type: "POST",
      url: path + "/cdn/media/popup-approval/" + id,
      success: function(data) {
        console.log(data);
        processDataPopup(data);
        console.log("API POST thành công");
      },
        error: function() {
          console.log("Lỗi khi gọi API POST");
        }
      });

    let popup = document.getElementById("myPopupApproval");
    popup.style.display = "block";
//    closePopupApproval2();
}

function closePopupApproval() {
    removeSelectElement();
    let popup = document.getElementById("myPopupApproval");
    popup.style.display = "none";
}


    function processDataPopup(data) {
        addId(data.data.id);
        addSrcImage(data.data.mediaImage);
        addPublishTime(data.data.publishTime);
        addMedia(data.data.cateId, data.data.mediaPath);
        addArrService(data.data.mediaServices);
        runSelect2Service();
    }

function removeSelectElement() {
  var selectElement = document.getElementById("serviceId");

  if (selectElement) {
    // Xóa thẻ select nếu nó tồn tại
    selectElement.remove();
  } else {
    console.log("Select element with id 'serviceId' not found.");
  }
}

function addSrcImage(mediaImage) {
    const imgElement = document.getElementById("dropdownMenu22");

        if (imgElement) {
            // Kiểm tra lỗi trước khi set attribute
            checkImage(domainNginx + mediaImage)
                .then(() => {
                    // Hình ảnh tải thành công, đặt giá trị cho thuộc tính src
                    imgElement.setAttribute("src", domainNginx + mediaImage);
                })
                .catch(() => {
                    // Có lỗi xảy ra, đặt giá trị mặc định nếu cần
                    console.log("Error loading image: " + domainNginx + mediaImage);
                    imgElement.setAttribute("src", path + "/img/default-zalo.jpg");
                });
        } else {
            console.log("Image element with id 'dropdownMenu22' not found.");
        }
}

function addPublishTime(publishTime) {
    document.getElementById("publishTime").value = publishTime;
}

function addId(id) {
    document.getElementById("id").value = id;
}

function addActived(actived) {
    document.getElementById("actived2").value = actived;
}

function addArrService(arrService) {
    var serviceIdDiv = document.getElementById("serviceIdDiv");
    var selectElement = document.createElement("select");
    selectElement.id = "serviceId";
    selectElement.name = "serviceId";
    selectElement.className = "form-control";
    selectElement.required = true;

    for(let i=0; i<arrService.length; i++) {
        var optionElement = document.createElement("option");
        optionElement.value = arrService[i].vcsService.id;
        optionElement.text = arrService[i].vcsService.name;
        selectElement.appendChild(optionElement);
    }
    serviceIdDiv.appendChild(selectElement);
}

function runSelect2Service() {
    $('#serviceId').select2({
        ajax: {
            type: 'GET',
            url: path + '/cdn/media/ajax-search-service',
            dataType: 'json',
            delay: 250,
            data: function(params) {
                return {
                        input_: params.term
                };
            },
            processResults: function(data) {
                return {
                    results: data
                };
            },
            cache: true
        },
        placeholder: select2ServiceId,
        minimumInputLength: 0,
        allowClear: true,
        multiple: true
    }).val(getSelectedValues('serviceId')).trigger('change');
}

function addMedia(cateId, mediaPath) {
console.log(cateId)
    if(cateId == "1" || cateId == "3") {
        document.getElementById("box_video_live").style.display = "block";
        document.getElementById("box_audio_live").style.display = "none";
        addVideo(mediaPath);
    } else if (cateId == "2" || cateId == "4") {
        document.getElementById("box_video_live").style.display = "none";
        document.getElementById("box_audio_live").style.display = "block";
        addAudio(mediaPath);
    }
}

function addVideo(mediaPath) {
    // Lấy đối tượng source theo id
    var sourceElement = document.getElementById("video_media_path");
    // Kiểm tra xem sourceElement có tồn tại hay không
    if (sourceElement) {
      // Đặt giá trị cho thuộc tính src
      sourceElement.setAttribute("src", mediaPath);
    } else {
      console.log("Source element not found.");
    }
}

function addAudio(mediaPath) {
    // Lấy đối tượng source theo id
        var sourceElement = document.getElementById("audio_media_path");
        // Kiểm tra xem sourceElement có tồn tại hay không
        if (sourceElement) {
          // Đặt giá trị cho thuộc tính src
          sourceElement.setAttribute("src", mediaPath);
        } else {
          console.log("Source element not found.");
        }
}

function checkImage(src) {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => resolve();
        img.onerror = () => reject();
        img.src = src;
    });
}