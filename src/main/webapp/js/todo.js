/*
 * ボタン押下時の処理
 */
function clickSubmitButton(actionValue) {
	// Formのaction
	$("form#todo>input[name='action']").val(actionValue);
	$("form#todo").submit();
}
