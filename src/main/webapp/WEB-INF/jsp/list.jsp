<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="test.entity.TodoUserEntity" %>
<%@ page import="test.entity.TodoTaskEntity" %>
<%@ page import="java.util.Calendar" %>
<%
	TodoUserEntity currentUser = (TodoUserEntity) request.getSession().getAttribute("currentUser");
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>作業一覧</title>
        <link rel="STYLESHEET" href="todo.css" type="${pageContext.request.contextPath}/css/text/css"/>
    </head>
    <body>
        <h1>作業一覧</h1>
        <hr>
        <div align="right">
            ようこそ <%= currentUser.getUserName() %> さん
        </div>
        <!-- 作業登録・検索 -->
        <table class="toolbar">
            <tr>
                <td>
                    <form action="todo" method="post">
                        <input type="hidden" name="action" value="add">
                        <input type="submit" value="作業登録">
                    </form>
                </td>
                <td align="right">
                    <form action="todo" method="post">
                        <input type="hidden" name="action" value="search">
                        <table>
                            <tr>
                                <td>
                                    検索キーワード
                                </td>
                                <td>
                                    <input type="text" name="keyword" value="" size="24">
                                </td>
                                <td>
                                    <input type="submit" value="検索">
                                </td>
                            </tr>
                        </table>
                    </form>
                </td>
            </tr>
        </table>
        <%
                TodoTaskEntity[] tasks = (TodoTaskEntity[]) request.getAttribute("tasks");
                if (tasks.length == 0) {
                    // アイテムが存在しない場合
        %>
        <div align="center">作業項目はありません。</div>
        <%

                } else {
        %>
        <table class="list">
            <tr>
                <th>
                    項目名
                </th>
                <th>
                    担当者
                </th>
                <th>
                    期限
                </th>
                <th>
                    完了
                </th>
                <th colspan="3">
                    操作
                </th>
            </tr>
            <%
                        // 現在時刻を取得(期限比較用: 分以降は0にリセット)
                                                                    Calendar calendar = Calendar.getInstance();
                                                                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                                                                    calendar.set(Calendar.MINUTE, 0);
                                                                    calendar.set(Calendar.SECOND, 0);
                                                                    calendar.set(Calendar.MILLISECOND, 0);
                                                                    long currentTime = calendar.getTimeInMillis();
                                                                    
                                                                    // アイテムを出力
                                                                    for(int i = 0; i < tasks.length; i ++) {
                                                                        TodoTaskEntity task = tasks[i];
                                                                        String styleAttr = "";	
                                                                        if(task.getFinishedDate() != null) {
                                                                            // 完了
                                                                            styleAttr = " style=\"background-color: #cccccc;\"";
                                                                        }else if(task.getUser().getId().equals(currentUser.getId())) {
                                                                            // 自分の作業
                                                                            styleAttr = " style=\"background-color: #ffbbbb;\"";
                                                                        }
                                                				if(task.getExpireDate().getTime() < currentTime && task.getFinishedDate() == null){
                                                					// 期限切れかつ未完了
                                                                            styleAttr += " style=\"color: #ff0000;\"";
                                                                        }
            %>
            <tr>
                <td <%=styleAttr%>>
                    <%= task.getTask() %>
                </td>
                <td <%=styleAttr%>>
                    <%=task.getUser().getUserName()%>
                </td>
                <td <%= styleAttr %>>
                    <%= task.getExpireDate() %>
                </td>
                <td <%= styleAttr %>><%
                        if(task.getFinishedDate() != null) {
                   	        %><%= task.getFinishedDate() %><%
                        }else{
                            %>未<%
                        }
                %></td>
                <td <%= styleAttr %> align="center">
                    <form action="todo" method="post">
                        <input type="hidden" name="action" value="finish">
                        <input type="hidden" name="task_id" value="<%= task.getId() %>">
                        <%
                            if(task.getFinishedDate() != null) {
                        %>
                        <input type="submit" value="未完了">
                        <%
                            }else{
                        %>
                        <input type="submit" value="完了">
                        <%
                            }
                        %>
                    </form>
                </td>
                <td <%= styleAttr %> align="center">
                    <form action="todo" method="post">
                        <input type="hidden" name="action" value="edit">
                        <input type="hidden" name="task_id" value="<%= task.getId() %>">
                        <input type="submit" value="更新">
                    </form>
                </td>
                <td <%= styleAttr %> align="center">
                    <form action="todo" method="post">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="task_id" value="<%= task.getId() %>">
                        <input type="submit" value="削除">
                    </form>
                </td>
            </tr>
            <%
                    }
            %>
        </table>
        <%
                }
        %>
    </body>
</html>