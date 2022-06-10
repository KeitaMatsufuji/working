<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ログイン</title>
        <link rel="STYLESHEET" href="${pageContext.request.contextPath}/css/todo.css" type="text/css">
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/login.js">
        </script>
    </head>
    <body onload="fieldChanged();">
        <h1>ログイン</h1>
        <hr>
        <div align="center">
            <form action="todo" method="post">
                <input type="hidden" name="action" value="login_action">
                <table>
                    <tr>
                        <th class="login_field">
                            ユーザID
                        </th>
                        <td class="login_field">
                            <input type="text" name="user_id" value="" 
							size="24" id="user_id" 
							onkeyup="fieldChanged();" 
							onchange="fieldChanged();">
                        </td>
                    </tr>
                    <tr>
                        <th class="login_field">
                            パスワード
                        </th>
                        <td class="login_field">
                            <input type="password" name="password" value="" 
							size="24" id="password" 
							onkeyup="fieldChanged();" 
							onchange="fieldChanged();">
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="login_button">
                            <input type="submit" value="ログイン" id="login">
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </body>
</html>