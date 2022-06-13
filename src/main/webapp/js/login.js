// フィールドが変更された場合に処理する関数
function fieldChanged(){
    const userId = getField("user_id");
    const password = getField("password");
    let disabled = true;
    
    if (userId.value.length > 0 && password.value.length > 0) {
        disabled = false;
    }
    
    const login = getField("login");
    if (disabled) {
        login.setAttribute("disabled", "disabled");
    }
    else {
        login.removeAttribute("disabled");
    }
}

// フィールドを取得する関数
function getField(fieldName){
    const field = document.getElementById(fieldName);
    if (field == undefined) {
        throw new Error("要素が見つかりません: " + fieldName);
    }
    return field;
}
