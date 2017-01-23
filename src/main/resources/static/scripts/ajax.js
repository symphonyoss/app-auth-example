function Ajax () {
}

Ajax.prototype = {
    call : function(url, data, method, contentType) {
        data = (data === undefined)?{}:data;

        var deferred = Q.defer();
        var type = 'json';

        method = (method=== undefined)?'POST':method;

        $.ajax({
            data: data,
            dataType: type,
            contentType: contentType?contentType:undefined,
            error: this.onAjaxError.bind(this, deferred),
            success: this.onAjaxSuccess.bind(this, deferred),
            type: method,
            url: which,
            xhrFields: {
                withCredentials: true
            }
        });

        return deferred.promise;
    }

    onAjaxSuccess : function(deferred, response, status, xhr)
    {
        deferred.resolve(response);
    }

    onAjaxError : function(deferred, jqXHR, textStatus, errorThrown)
    {
        deferred.reject(jqXHR);
    }
}

var ajax = new Ajax();
