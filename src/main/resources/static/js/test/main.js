// Hàm JavaScript thực hiện một số tác vụ cho phần tử có id là "myChartTotalView"
function doSomethingWithButton1() {
  // Lấy reference tới element canvas trong HTML
  var ctx = document.getElementById('myChartTotalView').getContext('2d');

  function getDataByDate(startDate, endDate, livestream) {
    var data = {};
    var currentDate = new Date(startDate);
    while (currentDate <= new Date(endDate)) {
      var dateString = currentDate.toDateString();
      data[dateString] = getRandomViewCount(livestream, currentDate);
      currentDate.setDate(currentDate.getDate() + 1);
    }
    return data;
  }

  function getRandomViewCount(livestream, currentDate) {
    // Tìm số lượng view tương ứng với mỗi ngày từ livestream
    var totalViews = 0;
    for (var i = 0; i < livestream.length; i++) {
      var item = livestream[i];
      var itemDate = new Date(item.dateStart).toDateString();
      if (itemDate === currentDate.toDateString()) {
        totalViews += item.totalView;
      }
    }

    // Giả định một hàm để sinh số lượng view ngẫu nhiên dựa trên tổng số view
    // trong khoảng 70% đến 100% của tổng số view
    var minPercentage = 0.7;
    var maxPercentage = 1.0;
    var randomPercentage = Math.random() * (maxPercentage - minPercentage) + minPercentage;
    // var randomViews = Math.floor(totalViews * randomPercentage);
    var randomViews = Math.floor(totalViews);

    return randomViews;
  }

  var dataByDate = getDataByDate(startDate, endDate, livestream);

  var dateLabels = Object.keys(dataByDate).map(function(dateString) {
    var dateParts = dateString.split(' ');
    var dateObject = new Date(dateParts[1] + ' ' + dateParts[2] + ', ' + dateParts[3]);
    var formattedDate = moment(dateObject).format('DD-MM-YYYY');
    return formattedDate;
  });

  var data = {
    // labels: Object.keys(dataByDate), // Lấy các ngày từ object dataByDate
    labels: dateLabels, // Lấy các ngày từ object dataByDate
    datasets: [{
      label: totalViews,
      data: Object.values(dataByDate), // Lấy các số lượng view từ object dataByDate
      fill: false,
      borderColor: 'rgba(255, 99, 132, 1)',
      borderWidth: 1
    }]
  };

// Cấu hình biểu đồ
  var options = {
    scales: {
      x: {
        display: true,
        title: {
          display: true,
          text: dateFormat
        }
      },
      y: {
        display: true,
        min: 0,
        title: {
          display: true,
          text: totalViews
        }
      }
    }
  };

// Tạo biểu đồ dạng line bằng Chart.js
  var myChartTotalView = new Chart(ctx, {
    type: 'line',
    data: data,
    options: options
  });
}

function doSomethingWithButton2() {
  // Lấy reference tới element canvas trong HTML
  var ctx = document.getElementById('myChartTotalLives').getContext('2d');

  function getDataByDate(startDate, endDate, totallivestream) {
    var data = {};
    var currentDate = new Date(startDate);
    while (currentDate <= new Date(endDate)) {
      var dateString = currentDate.toDateString();
      data[dateString] = getRandomViewCount(totallivestream, currentDate);
      currentDate.setDate(currentDate.getDate() + 1);
    }
    return data;
  }

  function getRandomViewCount(totallivestream, currentDate) {
    // Tìm số lượng view tương ứng với mỗi ngày từ livestream
    var totalViews = 0;
    for (var i = 0; i < totallivestream.length; i++) {
      var item = totallivestream[i];
      var itemDate = new Date(item.dateStart).toDateString();
      if (itemDate === currentDate.toDateString()) {
        totalViews += item.totalLivestream;
      }
    }
    var randomViews = Math.floor(totalViews);
    return randomViews;
  }

  var dataByDate = getDataByDate(startDate, endDate, totallivestream);

  var dateLabels = Object.keys(dataByDate).map(function(dateString) {
    var dateParts = dateString.split(' ');
    var dateObject = new Date(dateParts[1] + ' ' + dateParts[2] + ', ' + dateParts[3]);
    var formattedDate = moment(dateObject).format('DD-MM-YYYY');
    return formattedDate;
  });

  var data = {
    labels: dateLabels,
    datasets: [{
      label: totalLivestream,
      data: Object.values(dataByDate),
      fill: false,
      borderColor: 'rgb(50,117,71)',
      borderWidth: 1
    }]
  };

// Cấu hình biểu đồ
  var options = {
    scales: {
      x: {
        display: true,
        title: {
          display: true,
          text: dateFormat
        }
      },
      y: {
        display: true,
        min: 0,
        title: {
          display: true,
          text: totalLivestream
        }
      }
    }
  };

// Tạo biểu đồ dạng line bằng Chart.js
  var myChartTotalLives = new Chart(ctx, {
    type: 'line',
    data: data,
    options: options
  });
}

function doSomethingWithButton3() {
  var ctx = document.getElementById('myChartTotalTime').getContext('2d');

  function getDataByDate(startDate, endDate, totaltime) {
    var data = {};
    var currentDate = new Date(startDate);
    while (currentDate <= new Date(endDate)) {
      var dateString = currentDate.toDateString();
      data[dateString] = getRandomViewCount(totaltime, currentDate);
      currentDate.setDate(currentDate.getDate() + 1);
    }
    return data;
  }

  function getRandomViewCount(totaltime, currentDate) {
    // Tìm số lượng view tương ứng với mỗi ngày từ livestream
    var totalViews = 0;
    if (totaltime.length > 0){
      for (var i = 0; i < totaltime.length; i++) {
        var item = totaltime[i];
        var itemDate = new Date(item.dateStart).toDateString();
        if (itemDate === currentDate.toDateString()) {
          totalViews += item.totalTime;
        }
      }
    }
    var randomViews = Math.floor(totalViews);
    return randomViews;
  }

  var dataByDate = getDataByDate(startDate, endDate, totaltime);

  var dateLabels = Object.keys(dataByDate).map(function(dateString) {
    var dateParts = dateString.split(' ');
    var dateObject = new Date(dateParts[1] + ' ' + dateParts[2] + ', ' + dateParts[3]);
    var formattedDate = moment(dateObject).format('DD-MM-YYYY');
    return formattedDate;
  });

  var data = {
    labels: dateLabels,
    datasets: [{
      label: totalTimeLivestream,
      data: Object.values(dataByDate),
      fill: false,
      borderColor: 'rgb(10,157,203)',
      borderWidth: 1
    }]
  };

// Cấu hình biểu đồ
  var options = {
    scales: {
      x: {
        display: true,
        title: {
          display: true,
          text: dateFormat
        }
      },
      y: {
        display: true,
        min: 0,
        title: {
          display: true,
          text: totalTimeLivestream
        }
      }
    }
  };

// Tạo biểu đồ dạng line bằng Chart.js
  var myChartTotalTime = new Chart(ctx, {
    type: 'line',
    data: data,
    options: options
  });
}

window.onload = doSomethingWithButton1();
window.onload = doSomethingWithButton2();
window.onload = doSomethingWithButton3();
