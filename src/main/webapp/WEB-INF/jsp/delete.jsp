<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="test.entity.TodoTaskEntity" %>
<%
TodoTaskEntity task = (TodoTaskEntity) request.getAttribute("task");
%>
<!DOCTYPE html>
<html>
    <head>
      <meta charset="UTF-8">
      <title>削除確認</title>
      <link rel="STYLESHEET" href="todo.css" type="${pageContext.request.contextPath}/css/text/css"/>
    </head>
    <body>
        <h1>削除確認</h1>
        <hr>
        <div align="center">
            <table>
                <tr>
                    <td class="add_field">
                        項目 <%= task.getTask() %> を削除します。<br>
                        よろしいですか？
                    </td>
                </tr>
                <tr>
                    <td class="add_button">
                        <table>
                            <tr>
                                <td>
                                    <form action="todo" method="post">
                                        <input type="hidden" name="action" value="delete_action">
                                        <input type="hidden" name="task_id" value="<%= task.getId() %>">
                                        <input type="submit" value="削除">
                                    </form>
                                </td>
                                <td>
                                    <form action="todo" method="post">
                                        <input type="hidden" name="action" value="list">
                                        <input type="submit" value="キャンセル">
                                    </form>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
         </div>
     </body>
</html>
