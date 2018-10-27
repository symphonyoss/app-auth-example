function Ajax () {
}

Ajax.prototype = {
    call : function(url, data, method, contentType) {
        data = (data === undefined)?{}:data;

        var deferred = Q.defer();
        var type = 'json';

        method = (method=== undefined)?'POST':method;

        contentType = contentType || "";

        if (contentType.indexOf('json') !== -1) {
            data = JSON.stringify(data);
        }

        $.ajax({
            data: data,
            dataType: type,
            contentType: contentType?contentType:undefined,
            error: this.onAjaxError.bind(this, deferred),
            success: this.onAjaxSuccess.bind(this, deferred),
            type: method,
            url: url,
            xhrFields: {
                withCredentials: true
            }
        });

        return deferred.promise;
    },

    onAjaxSuccess : function(deferred, response, status, xhr)
    {
        deferred.resolve(response);
    },

    onAjaxError : function(deferred, jqXHR, textStatus, errorThrown)
    {
        deferred.reject(jqXHR);
    }
}

var ajax = new Ajax();
