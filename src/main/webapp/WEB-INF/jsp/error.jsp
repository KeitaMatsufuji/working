<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
      <meta charset="UTF-8">
      <title>エラー</title>
      <link rel="STYLESHEET" href="todo.css" type="${pageContext.request.contextPath}/css/text/css"/>
    </head>
    <body>
        <h1>エラー</h1>
        <hr>
        <div align="center">
            <form action="todo" method="get">
            	<input type="hidden" name="action" value="login">
                <table>
                    <tr>
                        <td class="add_field">
                            エラーが発生しました。<br>
                            内容: <%= request.getAttribute("message") %>
                        </td>
	                </tr>
                    <tr>
                        <td class="add_button">
                            <input type="submit" value="戻る">
                        </td>
                    </tr>
                </table>
            </form>
         </div>
     </body>
</html>
