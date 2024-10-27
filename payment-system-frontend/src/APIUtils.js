import { API_BASE_URL } from './constants';

export function initTransaction( data, token, merchantId) {
        return fetch(API_BASE_URL + "/api/v1/payment/transactions/init/"+merchantId, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(data)
        })
        .then(result => {
            if(result.status !== 201){
                return Promise.reject(result);
            }else{
                return result;
            }
        })
   
}

export function systemLogin(token) {
    return fetch(API_BASE_URL + "/api/v1/principal/signin", 
    {
        method: 'GET',
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(response.json);
        } else {
            return response.json();
        }
    }
    );
}


export async function parseReadableStreamToJson (stream) {
    const data = (await stream.getReader().read()).value
    const str = String.fromCharCode.apply(String, data);
    return str;
}

export async function changeStatusOfMerchant(merchantId, status, token){
    return fetch(API_BASE_URL + "/api/v1/admin/merchants/"+merchantId+"/status/"+status, {
        method: "PUT",
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(response);
        } else {
            return response.json();
        }
    });
}
export async function deletePrincipal(merchantId, token){
    return fetch(API_BASE_URL + "/api/v1/admin/merchants/"+merchantId, {
        method: "DELETE",
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(response);
        } else {
            return response;
        }
    });
}
export async function getAllMerchants(token){
    return fetch(API_BASE_URL + "/api/v1/admin/merchants", {
        method: "GET",
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(response);
        } else {
            return response.json();
        }
    });
}
export async function getMerchant(merchantId, token, limit, offset) {
    return fetch(API_BASE_URL + "/api/v1/merchants/"+ merchantId+"?limit="+limit+"&offset="+offset, {
        method: "GET",
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(response);
        } else {
            return response.json();
        }
    }
    );
}



