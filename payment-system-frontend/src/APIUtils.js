import { API_BASE_URL, ACCESS_TOKEN, PRINCIPAL } from './constants';

export function fetchAPI(methodType, url, data) {
        return fetch(url, {
            method: methodType,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
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

export function login(loginRequest) {
    return fetch(API_BASE_URL + "/api/v1/auth/signin", 
    {
        method: 'POST',
        headers: {
            'Content-type': 'application/json',
        },
        body: JSON.stringify(loginRequest)
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(response.json);
        } else {
            return response.json();
        }
    }
    );
}

export function importPrincipals(formData){
    return fetch(API_BASE_URL + "/api/v1/import/principals", {
        method: "POST",
        body: formData
    }).then(response => {
        if (response.status !== 202) {
            return Promise.reject(response);
        } else {
            return response;
        }
    }
    );

}

export async function parseReadableStreamToJson (stream) {
    const data = (await stream.getReader().read()).value
    const str = String.fromCharCode.apply(String, data);
    return str;
}

export async function changeStatusOfMerchant(merchantId, status){
    return fetch(API_BASE_URL + "/api/v1/admin/merchants/"+merchantId+"/status/"+status, {
        method: "PUT",
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
        }
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(response);
        } else {
            return response;
        }
    });
}
export async function deletePrincipal(merchantId){
    return fetch(API_BASE_URL + "/api/v1/admin/merchants/"+merchantId, {
        method: "DELETE",
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
        }
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(response);
        } else {
            return response;
        }
    });
}
export async function getAllMerchants(){
    return fetch(API_BASE_URL + "/api/v1/admin/merchants", {
        method: "GET",
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
        }
    }).then(response => {
        if (!response.ok) {
            return Promise.reject(response);
        } else {
            return response.json();
        }
    });
}
export async function getMerchant(merchantId) {
    return fetch(API_BASE_URL + "/api/v1/admin/merchants/"+ merchantId, {
        method: "GET",
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
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

export function getME() {
    return fetch(API_BASE_URL + "/api/v1/auth/whoami", {
        method: "GET",
        headers: {
            'Content-type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
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


export function getCurrentUser() {

    if (!localStorage.getItem(ACCESS_TOKEN)) {
        return Promise.reject("No access token set.");
    }

    return getME();
}
