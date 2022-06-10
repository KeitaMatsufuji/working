<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="test.entity.TodoUserEntity" %>
<%@ page import="test.entity.TodoTaskEntity" %>
<%@ page import="java.util.Calendar" %>
<%
    TodoTaskEntity task = (TodoTaskEntity) request.getAttribute("task");
	TodoUserEntity[] users = (TodoUserEntity[]) request.getAttribute("users");
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>作業更新</title>
        <link rel="STYLESHEET" href="todo.css" type="${pageContext.request.contextPath}/css/text/css"/>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.6.0.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/todo.js"></script>
    </head>
    <body>
        <h1>作業更新</h1>
        <hr>
        <div align="center">
            <form action="todo" method="post" id="todo">
                <input type="hidden" name="action">
                <input type="hidden" name="task_id" value="<%=task.getId()%>">
                <table>
                    <tr>
                        <th class="add_field">
                            項目名
                        </th>
                        <td class="add_field">
                            <input type="text" name="name" value="<%=task.getTask()%>" size="24">
                        </td>
                    </tr>
                    <tr>
                        <th class="add_field">
                            担当者
                        </th>
                        <td class="add_field">
                            <select name="user_id" size="1">
                            <%
                                for(int i = 0; i < users.length; i ++) {
                                    TodoUserEntity user = users[i];
                            %>
                                    <option value="<%= user.getId() %>" <%
                                    if(user.getId().equals(task.getUser().getId())) {
                                        %>selected<%
                                    }
                                %>><%=user.getUserName()%></option>
                            <%
                                }
                            %>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <th class="add_field">
                            期限
                        </th>
                        <td class="add_field">
                        <%
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(task.getExpireDate());
                        %>
                            <input type="text" name="year" value="<%= calendar.get(Calendar.YEAR) %>" size="8">/<input type="text" name="month" value="<%= calendar.get(Calendar.MONTH) + 1 %>" size="4">/<input type="text" name="day" value="<%= calendar.get(Calendar.DAY_OF_MONTH) %>" size="4">
                        </td>
                    </tr>
                    <tr>
                        <th class="add_field">
                            完了
                        </th>
                        <td class="add_field">
                            <input type="checkbox" name="finished" value="true" size="8"<%
                                if(task.getFinishedDate() != null) {
                                    %>checked<%
                                }
                            %>>完了した
                        </td>
                    </tr>
                </table>
                <div>
                    <input type="button" value="更新" onClick="clickSubmitButton('edit_action');">
                    <input type="button" value="キャンセル" onClick="clickSubmitButton('list');">
                </div>
            </form>
        </div>
    </body>
</html>
