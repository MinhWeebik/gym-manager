MyConstant = function(options){
    console.log("init MyConstant class");
    this.options = options;
};
MyConstant.prototype = {
    getParameter: function(key) {
        console.log("Get constant|" + key);
        var hashTable = new Array();
        hashTable["IMAGE_DOMAIN"] = "{{config('config.image_host')}}";
        hashTable["SEARCH_DOMAIN"] = "{{config('config.search_host')}}";
        hashTable["DEFAULT_IMAGE"] = "/v3/images/defaultImage.png";

        // Language
        hashTable["MSISDN_ERROR"] = lang['MSISDN_ERROR'];
        hashTable["NEWS_DELETED"] = lang['NEWS_DELETED'];
        hashTable["SELECT_NEWS_DELETE"] = lang['SELECT_NEWS_DELETE'];
        hashTable["SELECT_NEWS_ONLYONE"] = lang['SELECT_NEWS_ONLYONE'];
        hashTable["ERROR"] = lang['ERROR'];
        hashTable["ERROR_ID"] = lang['ERROR_ID'];

        hashTable["ERROR_401"] = lang['ERROR'];
        hashTable["ERROR_403"] = lang['ERROR_403'];
        hashTable["ERROR_404"] = lang['ERROR_404'];
        hashTable["ERROR_500"] = lang['ERROR_500'];
        hashTable["ERROR_OTHER"] = lang['ERROR_OTHER'];
        hashTable["FILTER_SUCCESS"] = lang['FILTER_SUCCESS'];

        return hashTable[key];
    },
    echo: function(key) {
        return key;
    }
}

var myConstant = new MyConstant();
console.log(myConstant.getParameter("IMAGE_DOMAIN"));
console.log(myConstant.getParameter("SEARCH_DOMAIN"));
