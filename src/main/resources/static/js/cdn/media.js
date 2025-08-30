$(document).ready(function() {
    var selectElementCateId = document.getElementById('cateId').value; // Lấy tham chiếu đến phần tử <select>
    var selectElementIsEpisode = document.getElementById('isEpisode').value;
    if (selectElementCateId == 1) {
        disableElements();
        document.getElementById("profile-film").style.display = "block";
        document.getElementById("episode-div").style.display = "block";
        document.getElementById("subtitle-div").style.display = "block";
        document.getElementById("media-file-div").style.display = "block";
        document.getElementById("categoryTypeId").removeAttribute("disabled");
    }
    else if (selectElementCateId == 2) {
        disableElements();
        document.getElementById("profile-audio").style.display = "block";
        document.getElementById("media-file-div").style.display = "block";
        document.getElementById("categoryTypeId").removeAttribute("disabled");
    }
    else if (selectElementCateId == 3) {
        disableElements();
        document.getElementById("profile-film").style.display = "block";
        document.getElementById("media-file-div").style.display = "block";
        document.getElementById("categoryTypeId").removeAttribute("disabled");
    }
    else {
        disableElements();
    }

    if(selectElementIsEpisode == 1) {
        $("#profile-film").css("display", "none")
        $("#media-file-div").css("display", "none")
        $("#subtitle-div").css("display", "none")
    } else {
        $("#profile-film").css("display", "block")
        $("#media-file-div").css("display", "block")
        $("#subtitle-div").css("display", "block")
    }
    $("#isEpisode").change(function(e){
        if(e.target.value == 1) {
            //document.getElementById("profile-film").style.display = "block";
            $("#profile-film").css("display", "none")
            $("#media-file-div").css("display", "none")
            $("#subtitle-div").css("display", "none")
        } else {
            $("#profile-film").css("display", "block")
            $("#media-file-div").css("display", "block")
            $("#subtitle-div").css("display", "block")
        }
    });
    $("#isCopyright").change(function(e) {
        if(e.target.value == 1) {
            $("#copyright-value-div").css("display", "block")
        } else {
            $("#copyright-value-div").css("display", "none")
        }
    });

    $("#cateId").on("select2:select", function (e) {
        contentType = document.getElementById("cateId").value;
        runSelect2(contentType);
        $('#categoryTypeId').val(null).trigger('change');
        let selectedValue = e.params.data.id; // Lấy giá trị đã chọn
        let selectElement = document.getElementById("categoryTypeId")
        console.log("Giá trị đã chọn: " + selectedValue);
        if (selectedValue == 1) {
            disableElements();
            document.getElementById("profile-film").style.display = "block";
            document.getElementById("episode-div").style.display = "block";
            document.getElementById("subtitle-div").style.display = "block";
            document.getElementById("media-file-div").style.display = "block";
            selectElement.removeAttribute("disabled");
        }
        else if (selectedValue == 2) {
            disableElements();
            document.getElementById("profile-audio").style.display = "block";
            document.getElementById("media-file-div").style.display = "block";
            selectElement.removeAttribute("disabled");
        }
        else if (selectedValue == 3) {
            disableElements();
            document.getElementById("profile-film").style.display = "block";
            document.getElementById("media-file-div").style.display = "block";
            selectElement.removeAttribute("disabled");
        }
        else {
            disableElements();
        }
    });

    function disableElements() {
        let selectElement = document.getElementById("categoryTypeId")
        document.getElementById("profile-film").style.display = "none";
        document.getElementById("profile-audio").style.display = "none";
        document.getElementById("episode-div").style.display = "none";
        document.getElementById("subtitle-div").style.display = "none";
        document.getElementById("media-file-div").style.display = "none";
        selectElement.setAttribute("disabled", "disabled");
    }

function runSelect2(contentType) {
$('#categoryTypeId').select2({
            ajax: {
                type: 'GET',
                url: path + '/cdn/media/ajax-search?contentType=' + contentType,
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
            placeholder: select2cateType,
            minimumInputLength: 0,
            allowClear: true
        });
}
});