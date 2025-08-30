
var urlChartConfig = {
    'CHART_TOTAL_VIEW' : '/chart/total_view',
    'CHART_CHAT' : '/chart/chat',
    'CHART_AVERAGE_WATCH_TIME' : '/chart/average_watch_time',
    'CHART_SEEN_NUMBER' : '/chart/seen_number'
}

var urlChart = null;

$('#cars_number_view').on('change', function () {
    var id_live = $('#id_live').val();
    var type = this.value;
    getData(id_live, type);
});

function getData(id_live, type) {
    if(type == undefined) {
        type = $('#cars_number_view').val();
        id_live = $('#id_live').val();
    }

    var maxXAxes = 10;
    var maxYAxes = 5;
    switch(type) {
        case "1":
            urlChart = urlChartConfig.CHART_SEEN_NUMBER;
            break;
        case "2":
            urlChart = urlChartConfig.CHART_CHAT;
            break;
        case "3":
            urlChart = urlChartConfig.CHART_TOTAL_VIEW
            break;
        case "4":
            urlChart = urlChartConfig.CHART_AVERAGE_WATCH_TIME
            break;
        default:
            urlChart = urlChartConfig.CHART_TOTAL_VIEW
    }


    //Todo: set maxYAxes theo max value
    //Todo: tinh chinh thanh player
    //Todo: 4 so hien thi call qua api, cu 30s call 1 lan

    $.ajax({
        url: domainUrl + urlChart,
        method: 'GET',
        data: {
            id: id_live,
        },
        success: function (res) {
            let data_value = [];
            let myData = res.data.data;

            if (type == 4) { //CHART_AVERAGE_WATCH_TIME
                Object.keys(myData).forEach(function(key, index) {
                     myData[key] = myData[key] / 1000;
                });
            }

            data_value = Object.values(myData);

            maxYVal = Math.max(...data_value);
            maxYAxes = Math.ceil(Math.max(...data_value)*120/100);
            stepY = Math.ceil(maxYAxes/10);

            if(maxYAxes == 0){
                maxYAxes = 5;
                stepY = 1;
            }
            console.log(res);

            var ctxL = document.getElementById("lineChart").getContext('2d');
            new Chart(ctxL, {
                type: "line",
                data: {
                    labels: Object.keys(myData),
                    datasets: [{
                        fill: false,
                        lineTension: 0,

                        backgroundColor: [
                            'rgba(0, 137, 132, .2)',
                        ],
                        borderColor: [
                            'rgba(0, 10, 130, .7)',
                        ],
                        borderWidth: 1,
                        data: data_value
                    }]
                },
                options: {
                    responsive: true,
                    legend: {display: false},
                    scales: {
                        yAxes: [{
                            ticks: {
                                min: 0,
                                max:maxYAxes,
                                stepSize: stepY,
                            }
                        }],
                        xAxes: [{
                            ticks: {
                                min: 0,
                                max:10,
                                stepSize: 0.5,
                            }
                        }],
                    },
                    tooltips: {
                        callbacks: {
                            label: function (tooltipItem) {

                                return tooltipItem.yLabel;
                            }
                        }
                    }
                }
            });
        },
        error: function () {
            // alert(myConstant.getParameter("ERROR"))
            console.log("get chart failed");
        }
    });
}

getData();

setInterval(getData, 30000);

function findValue(haystack) {
    for (const item of haystack) {
        if (item == 0) {
            return true;
        }
    }
    return false;
}

getnumberViewerApiData(id_live);
setInterval(getnumberViewerApiData(id_live), 30000);

function getnumberViewerApiData(id_live) {

    
    $.ajax({
        url: domainUrl+"/chart/stat?id=" + id_live,
        method: 'GET',
        data: {
            id_live: id_live,
        },
        success: function (data) {

            console.log("getnumberViewerApiData", data);
            if(data.code == 200) {
                const dataInfo = data.data;
                $("#number_view_simul").html(dataInfo.seen_number);
                $("#chat_ratio").html(dataInfo.total_chat);
                $("#transmitters").html(dataInfo.total_view);
                $("#average_view").html((Math.ceil(dataInfo.average_watch_time/1000/60) + " m"));
            }
            
        },
        error: function () {
            // alert(myConstant.getParameter("ERROR"))
            console.log(domainUrl+"/chart/stat?id=" + id_live);
            console.log("get 4 info failed");
        }
    });
}
