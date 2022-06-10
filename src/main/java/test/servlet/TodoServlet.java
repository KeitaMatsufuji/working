package test.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import test.entity.TodoTaskEntity;
import test.entity.TodoUserEntity;

/**
 * TODO管理をおこなうサーブレット。
 * (全画面)
 */
@WebServlet("/todo")
public class TodoServlet extends HttpServlet {

	/**
	 * シリアルバージョンUID(おまじない)
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * JSPのベースディレクトリ。
	 */
	private static final String JSP_BASE = "/WEB-INF/jsp/";

	/**
	 * データベース接続先のURL
	 * 環境などにより値が変わる可能性があるものは、定数として定義します。
	 */
	private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/orclpdb";
	
	/**
	 * データベース接続時のユーザー名
	 * 環境などにより値が変わる可能性があるものは、定数として定義します。
	 */
	private static final String DB_USER = "admin";
	
	/**
	 * データベース接続時のパスワード 
	 * 環境などにより値が変わる可能性があるものは、定数として定義します。
	 */
	private static final String DB_PASSWORD = "admin";
	
	/**
	 * データベースのコネクションを保持します。
	 */
	private Connection _pooledConnection;

	/**
	 * 構築します。
	 */
	public TodoServlet() {
		_pooledConnection = null;
	}

	@Override
	public void destroy() {
		if (_pooledConnection != null) {
			try {
				_pooledConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			_pooledConnection = null;
		}

		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// 要求からactionパラメータを取得
		String action = req.getParameter("action");

		/* -----------------------------------------------
		 * ●Null例外を発生させない方法の一つとして「そもそもNULLを設定しない」というものがあります。
		 * 
		 * データベースと連携する等「Null」が意味を持つ場合を除き、必ず何かしらの値が入っていることが期待されている処理であるなら
		 * デフォルト値を決めておくと良いです。
		 * -----------------------------------------------*/
		// ログイン画面の処理
		// login.jspへフォワードする
		String forward = JSP_BASE + "login.jsp";
		if (!"login".equals(action)) {
			// 不正なアクションの場合
			forward = doError(req, resp, "不正なリクエストです");
		}

		// JSPへのフォワード
		RequestDispatcher dispatcher = req.getRequestDispatcher(forward);
		dispatcher.forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// リクエストパラメータの文字コード指定(日本語による文字化け回避)
		request.setCharacterEncoding("UTF-8");
		//送信する文字コード指定(日本語による文字化け回避)
		response.setContentType("text/html;charset=UTF-8");
		// 要求からactionパラメータを取得
		String action = request.getParameter("action");
		if (action == null) {
			// JSPへのフォワード
			RequestDispatcher dispatcher = request.getRequestDispatcher(doError(request, response, "不正なリクエストです"));
			dispatcher.forward(request, response);
			return;
		}
		
		/* ----------------------------------------
		 * ●Java7からswitchにString型が、Java14からswitch式が使用できるようになりました。
		 * 条件によって求める値が異なる場合、if～elseの羅列よりもswitch式を使うことで見通しがよくなります。
		 * 
		 * プログラムでバグを出さないコツは「処理を書かない」です。
		 * 実行速度を重視する箇所でない場合はコード量が少なくなるほうを選ぶとよいです。
		 * ---------------------------------------- */
		String forward = switch(action) {	
		// ログイン画面からの入力受付
		case "login_action" -> doLoginAction(request, response);
		// 一覧画面の処理
		case "list" -> doListView(request, response);
		// 登録画面の処理
		case "add" -> doAddView(request, response);
		// 登録画面からの入力受付
		case "add_action" -> doAddAction(request, response);
		// 完了処理
		case "finish" -> doFinishAction(request, response);
		// 検索画面の処理
		case "search" -> doSearchView(request, response); 
		// 削除画面の処理
		case "delete" -> doDeleteView(request, response);
		// 削除画面からの入力受付
		case "delete_action" -> doDeleteAction(request, response);
		// 更新画面の処理
		case "edit" -> doEditView(request, response);
		// 更新画面からの入力受付
		case "edit_action" -> doEditAction(request, response);
		// 不正なアクションの場合
		default -> doError(request, response, "不正なリクエストです");
		};

		// JSPへのフォワード
		RequestDispatcher dispatcher = request.getRequestDispatcher(forward);
		dispatcher.forward(request, response);
	}

	/**
	 * ログイン処理をおこないます。
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doLoginAction(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		String userID = req.getParameter("user_id");
		String password = req.getParameter("password");
		if (userID == null || password == null) {
			throw new ServletException("不正なパラメータです。");
		}

		try {
			// ユーザを取得する
			Optional<TodoUserEntity> user = getUser(userID, password);
			if (user.isEmpty()) {
				return doError(req, resp, "不正なユーザIDもしくはパラメータです。");
			}

			// 名前をセッションに格納する
			req.getSession().setAttribute("currentUser", user.get());

			// タスクを取得する
			TodoTaskEntity[] tasks = getTasks();

			// タスクを要求オブジェクトに格納する
			req.setAttribute("tasks", tasks);

			// 一覧を表示する
			return JSP_BASE + "list.jsp";
		} catch (SQLException e) {
			return doError(req, resp, e.getMessage());
		}
	}
	
	/**
	 * 一覧を表示します。
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doListView(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String forward = JSP_BASE + "list.jsp";
		// 一覧画面の処理
		try {
			TodoTaskEntity[] tasks = getTasks();
			request.setAttribute("tasks", tasks);
		} catch (SQLException e) {
			forward = doError(request, response, e.getMessage());
		}
		return forward;
	}
	
	/**
	 * 登録画面を表示します。
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doAddView(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String forward = JSP_BASE + "add.jsp";
		// 登録画面の処理
		try {
			TodoUserEntity[] users = getUsers();
			request.setAttribute("users", users);
		} catch (SQLException e) {
			forward = doError(request, response, e.getMessage());
		}
		return forward;
	}

	/**
	 * 登録要求を処理します。
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doAddAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			String name = req.getParameter("name");
			String userID = req.getParameter("user_id");
			String expireYear = req.getParameter("year");
			String expireMonth = req.getParameter("month");
			String expireDay = req.getParameter("day");
			if (name == null || userID == null || expireYear == null
					|| expireMonth == null || expireDay == null) {
				return doError(req, resp, "不正なパラメータです。");
			}
			// name = new String(name.getBytes("iso-8859-1"), "utf-8");
			
			/* -----------------------------------------
			 * ●Optioinalによる判定に変更します。
			 * ----------------------------------------- */
			Optional<TodoUserEntity> userResult = getUser(userID);
			if (userResult.isEmpty()) {
				return doError(req, resp, "不正なパラメータです。");
			}
			
			TodoUserEntity user = userResult.get();
			TodoTaskEntity targetItem = new TodoTaskEntity();
			targetItem.setUser(user);
			
			try {
				Date expireDate = getDate(Integer.valueOf(expireYear), Integer.valueOf(expireMonth), Integer.valueOf(expireDay));
				targetItem.setExpireDate(expireDate);
			} catch (NumberFormatException e) {
				return doError(req, resp, "不正なパラメータです。");
			}
			targetItem.setTask(name);

			executeUpdate(createInsertSQL(targetItem));

			// タスクを取得する
			TodoTaskEntity[] tasks = getTasks();

			// タスクを要求オブジェクトに格納する
			req.setAttribute("tasks", tasks);

			// 一覧を表示する
			return JSP_BASE + "list.jsp";
		} catch (SQLException e) {
			return doError(req, resp, e.getMessage());
		}
	}

	/**
	 * 完了要求を処理します。
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doFinishAction(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			/* -----------------------------------------
			 * ●Nullが返される可能性があるのにNullチェックが行われていませんでした。
			 * Optioinalを使用し、Nullであった場合の処理を追加します。
			 * ----------------------------------------- */
			Optional<TodoTaskEntity> currentItemResult = getTask(req);
			if (currentItemResult.isEmpty()) {
				return doError(req, resp, "対象のタスクが存在しません。");
			}
			
			TodoTaskEntity currentItem = currentItemResult.get();
			if (currentItem.getFinishedDate() == null) {
				// 完了に変更
				Calendar calendar = Calendar.getInstance();
				currentItem
						.setFinishedDate(new Date(calendar.getTimeInMillis()));
			} else {
				// 未完了に変更
				currentItem.setFinishedDate(null);
			}

			int updateCount = executeUpdate(createUpdateSQL(currentItem));
			if (updateCount != 1) {
				return doError(req, resp, "更新に失敗しました。");
			}

			if (req.getParameter("keyword") == null) {
				// タスクを取得する
				TodoTaskEntity[] tasks = getTasks();

				// タスクを要求オブジェクトに格納する
				req.setAttribute("tasks", tasks);

				// 一覧を表示する
				return JSP_BASE + "list.jsp";
			}
			Optional<TodoTaskEntity[]> tasks = searchTasks(req);

			// タスクを要求オブジェクトに格納する
			req.setAttribute("tasks", tasks.get());

			// 一覧を表示する
			return JSP_BASE + "search.jsp";
		} catch (SQLException e) {
			return doError(req, resp, e.getMessage());
		}
	}

	/**
	 * 検索画面を表示します
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doSearchView(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 検索画面の処理
		String forward = JSP_BASE + "search.jsp";
		try {
			Optional<TodoTaskEntity[]> tasks = searchTasks(request);
			if (tasks.isEmpty()) {
				forward = doError(request, response, "不正なパラメータです。");
			} else {
				request.setAttribute("tasks", tasks.get());
			}
		} catch (SQLException e) {
			forward = doError(request, response, e.getMessage());
		}
		return forward;
	}
	
	/**
	 * 編集画面を表示します
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doEditView(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 更新画面の処理
		String forward = JSP_BASE + "edit.jsp";
		try {
			/* -----------------------------------------
			 * ●Optioinalによる判定に変更します。
			 * ----------------------------------------- */
			Optional<TodoTaskEntity> task = getTask(request);
			if (task.isEmpty()) {
				return doError(request, response, "不正なパラメータです。");
			}
			request.setAttribute("task", task.get());
			
			
			TodoUserEntity[] users = getUsers();
			request.setAttribute("users", users);
			
		} catch (SQLException e) {
			forward = doError(request, response, e.getMessage());
		}
		return forward;
	}
	
	/**
	 * 編集要求を処理します。
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doEditAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			String name = req.getParameter("name");
			String userID = req.getParameter("user_id");
			String expireYear = req.getParameter("year");
			String expireMonth = req.getParameter("month");
			String expireDay = req.getParameter("day");
			if (name == null || userID == null || expireYear == null
					|| expireMonth == null || expireDay == null) {
				return doError(req, resp, "不正なパラメータです。");
			}
			// name = new String(name.getBytes("iso-8859-1"), "utf-8");
			
			/* -----------------------------------------
			 * ●Nullが返される可能性があるのにNullチェックが行われていませんでした。
			 * Optioinalを使用し、Nullであった場合の処理を追加します。
			 * ----------------------------------------- */
			Optional<TodoTaskEntity> currentItemResult = getTask(req);
			if (currentItemResult.isEmpty()) {
				return doError(req, resp, "対象のタスクが存在しません。");
			}
			TodoTaskEntity currentItem = currentItemResult.get();
			
			Optional<TodoUserEntity> userResult = getUser(userID);
			if (userResult.isEmpty()) {
				return doError(req, resp, "ユーザーが存在しません。");
			}
			TodoUserEntity user = userResult.get();
			
			currentItem.setUser(user);
			
			try {
				Date expireDate = getDate(Integer.valueOf(expireYear), Integer.valueOf(expireMonth), Integer.valueOf(expireDay));
				currentItem.setExpireDate(expireDate);
			} catch (NumberFormatException e) {
				currentItem.setExpireDate(null);
			}
			
			currentItem.setTask(name);
			String finished = req.getParameter("finished");
			if ("true".equals(finished)) {
				if (currentItem.getFinishedDate() == null) {
					Calendar calendar = Calendar.getInstance();
					currentItem.setFinishedDate(new Date(calendar
							.getTimeInMillis()));
				}
			} else {
				currentItem.setFinishedDate(null);
			}

			int updateCount = executeUpdate(createUpdateSQL(currentItem));
			if (updateCount != 1) {
				return doError(req, resp, "更新に失敗しました。");
			}

			// タスクを取得する
			TodoTaskEntity[] tasks = getTasks();

			// タスクを要求オブジェクトに格納する
			req.setAttribute("tasks", tasks);

			// 一覧を表示する
			return JSP_BASE + "list.jsp";

		} catch (SQLException e) {
			return doError(req, resp, e.getMessage());
		}
	}

	/**
	 * 削除画面を表示します
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doDeleteView(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 削除画面の処理
		String forward = JSP_BASE + "delete.jsp";
		try {
			/* -----------------------------------------
			 * ●Optioinalによる判定に変更します。
			 * ----------------------------------------- */
			Optional<TodoTaskEntity> task = getTask(request);
			if (task.isEmpty()) {
				return doError(request, response, "不正なパラメータです。");
			}
			
			request.setAttribute("task", task.get());
		} catch (SQLException e) {
			forward = doError(request, response, e.getMessage());
		}
		return forward;
	}
	
	/**
	 * 削除要求を処理します。
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doDeleteAction(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			/* -----------------------------------------
			 * ●Nullが返される可能性があるのにNullチェックが行われていませんでした。
			 * Optioinalを使用し、Nullであった場合の処理を追加します。
			 * ----------------------------------------- */
			Optional<TodoTaskEntity> currentItem = getTask(req);
			if (currentItem.isEmpty()) {
				return doError(req, resp, "対象のタスクが存在しません。");
			}

			int updateCount = executeUpdate(createDeleteSQL(currentItem.get()));
			if (updateCount != 1) {
				return doError(req, resp, "更新に失敗しました。");
			}

			// タスクを取得する
			TodoTaskEntity[] tasks = getTasks();

			// タスクを要求オブジェクトに格納する
			req.setAttribute("tasks", tasks);

			// 一覧を表示する
			return JSP_BASE + "list.jsp";
		} catch (SQLException e) {
			return doError(req, resp, e.getMessage());
		}
	}

	/**
	 * エラーを表示します。
	 * 
	 * @param req
	 * @param resp
	 * @param message
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private String doError(HttpServletRequest req, HttpServletResponse resp,
			String message) throws ServletException, IOException {
		req.setAttribute("message", message);

		// エラーを表示する
		return JSP_BASE + "error.jsp";
	}

	/**
	 * 接続オブジェクトを生成します。
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException {
		// Connectionの準備
		if (_pooledConnection != null) {
			return _pooledConnection;
		}
		try {
			// 下準備
			Class.forName("oracle.jdbc.driver.OracleDriver");
			_pooledConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			return _pooledConnection;
		} catch (ClassNotFoundException e) {
			_pooledConnection = null;
			throw new SQLException(e);
		} catch (SQLException e) {
			_pooledConnection = null;
			throw e;
		}
	}

	/**
	 * ユーザを取得します。
	 * 
	 * ●Java8からNull安全を目指すためにOptionalが導入されています。
	 * メソッドの戻り値として「Null」をエラーとしてではなく値がない状態として扱うのであれば、Optionalを使用します。
	 * 
	 * @param userID
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	private Optional<TodoUserEntity> getUser(String userID, String password) throws SQLException {
		/* -------------------------------------
		 * ●StatementクラスはAutoClosableインターフェースを継承しているので、tryの終了と同時に自動でclose処理が行われます。
		 *   そのため、finallyによるclose処理の記載が不要になります。
		 * ------------------------------------- */
		try (Statement statement = getConnection().createStatement();) {
			// SQL文を発行
			ResultSet resultSet = statement
					.executeQuery("SELECT ID,USER_NAME FROM TODO_USER WHERE ID='"
							+ userID + "' AND PASSWORD='" + password + "'");
			boolean br = resultSet.next();
			if (br == false) {
				// 検索結果が0件の場合
				return Optional.empty();
			}
			TodoUserEntity user = new TodoUserEntity();
			user.setId(resultSet.getString("ID"));
			user.setUserName(resultSet.getString("USER_NAME"));

			return Optional.of(user);
		} catch (SQLException e) {
			_pooledConnection = null;
			throw e;
		}
	}

	/**
	 * ユーザを取得します。
	 * 
	 * ●Java8からNull安全を目指すためにOptionalが導入されています。
	 * メソッドの戻り値として「Null」をエラーとしてではなく値がない状態として扱うのであれば、Optionalを使用します。
	 * 
	 * @param userID
	 * @return
	 * @throws ServletException
	 */
	private Optional<TodoUserEntity> getUser(String userID) throws SQLException {
		try (Statement statement = getConnection().createStatement();) {
			// SQL文を発行
			
			ResultSet resultSet = statement
					.executeQuery("SELECT ID,USER_NAME FROM TODO_USER WHERE ID='"
							+ userID + "'");
			boolean br = resultSet.next();
			if (br == false) {
				// 検索結果が0件の場合
				return Optional.empty();
			}
			TodoUserEntity user = new TodoUserEntity();
			user.setId(resultSet.getString("ID"));
			user.setUserName(resultSet.getString("USER_NAME"));

			return Optional.of(user);
		} catch (SQLException e) {
			_pooledConnection = null;
			throw e;
		}
	}

	/**
	 * ユーザ一覧を取得します。
	 * 
	 * ●Nullが返らないことが確実であれば、戻り値にOptionalを指定する必要はありません。
	 * また、呼び出し元もNULLチェックが不要となる為、その分処理の記載量が減ります。
	 * 
	 * @return
	 * @throws ServletException
	 */
	private TodoUserEntity[] getUsers() throws SQLException {
		try(Statement statement = getConnection().createStatement();) {
			// SQL文を発行
			
			ResultSet resultSet = statement
					.executeQuery("SELECT ID,USER_NAME FROM TODO_USER");
			boolean br = resultSet.next();
			if (br == false) {
				// 検索結果が0件の場合
				return new TodoUserEntity[0];
			}
			
			List<TodoUserEntity> users = new ArrayList<TodoUserEntity>();
			do {
				TodoUserEntity user = new TodoUserEntity();
				user.setId(resultSet.getString("ID"));
				user.setUserName(resultSet.getString("USER_NAME"));
				users.add(user);
			} while (resultSet.next());

			return users.toArray(new TodoUserEntity[0]);
		} catch (SQLException e) {
			_pooledConnection = null;
			throw e;
		}
	}

	/**
	 * タスクを取得します。
	 * 
	 * @return
	 * @throws ServletException
	 */
	private TodoTaskEntity[] getTasks() throws SQLException {
		try(Statement statement = getConnection().createStatement();) {
			// SQL文を発行
			
			String sql = createSQL(null);
			ResultSet resultSet = statement.executeQuery(sql);
			boolean br = resultSet.next();
			if (br == false) {
				return new TodoTaskEntity[0];
			}
			
			List<TodoTaskEntity> tasks = new ArrayList<TodoTaskEntity>();
			do {
				TodoTaskEntity task = new TodoTaskEntity();
				task.setId(resultSet.getString("ID"));
				task.setTask(resultSet.getString("TASK"));
				TodoUserEntity user = new TodoUserEntity();
				user.setId(resultSet.getString("ID"));
				user.setUserName(resultSet.getString("USER_NAME"));
				task.setUser(user);
				task.setExpireDate(resultSet.getDate("EXPIRE_DATE"));
				task.setFinishedDate(resultSet.getDate("FINISHED_DATE"));

				tasks.add(task);
			} while (resultSet.next());

			return tasks.toArray(new TodoTaskEntity[0]);
		} catch (SQLException e) {
			_pooledConnection = null;
			throw e;
		}
	}

	/**
	 * タスクを取得します。
	 * 
	 * ●Java8からNull安全を目指すためにOptionalが導入されています。
	 * メソッドの戻り値として「Null」をエラーとしてではなく値がない状態として扱うのであれば、Optionalを使用します。
	 * 
	 * @param req
	 * @return
	 * @throws ServletException
	 */
	private Optional<TodoTaskEntity> getTask(HttpServletRequest req) throws SQLException {
		String taskId = req.getParameter("task_id");
		if (taskId == null) {
			// 検索結果が0件の場合
			return Optional.empty();
		}
		return getTask(taskId);
	}

	/**
	 * タスクを取得します。
	 * 
	 * ●Java8からNull安全を目指すためにOptionalが導入されています。
	 * メソッドの戻り値として「Null」をエラーとしてではなく値がない状態として扱うのであれば、Optionalを使用します。
	 * 
	 * @param id
	 * @return
	 * @throws ServletException
	 */
	private Optional<TodoTaskEntity> getTask(String id) throws SQLException {
		try(Statement statement = getConnection().createStatement();) {
			// SQL文を発行
			ResultSet resultSet = statement
					.executeQuery(createSQL("TT.ID='" + id + "'"));
			boolean br = resultSet.next();
			if (br == false) {
				// 検索結果が0件の場合
				return Optional.empty();
			}
			
			TodoTaskEntity task = new TodoTaskEntity();
			task.setId(resultSet.getString("ID"));
			task.setTask(resultSet.getString("TASK"));
			TodoUserEntity user = new TodoUserEntity();
			user.setId(resultSet.getString("USER_ID"));
			user.setUserName(resultSet.getString("USER_NAME"));
			task.setUser(user);
			task.setExpireDate(resultSet.getDate("EXPIRE_DATE"));
			task.setFinishedDate(resultSet.getDate("FINISHED_DATE"));

			return Optional.of(task);
		} catch (SQLException e) {
			_pooledConnection = null;
			throw e;
		}
	}

	/**
	 * タスクを検索します。
	 * 
	 * ●Java8からNull安全を目指すためにOptionalが導入されています。
	 * メソッドの戻り値として「Null」をエラーとしてではなく値がない状態として扱うのであれば、Optionalを使用します。
	 * 
	 * @return
	 * @throws ServletException
	 */
	private Optional<TodoTaskEntity[]> searchTasks(HttpServletRequest req) throws SQLException,
			IOException {
		String keyword = req.getParameter("keyword");
		if (keyword == null) {
			return Optional.empty();
		}

		StringBuilder where = new StringBuilder();
		String[] fields = new String[] {"TT.TASK", "TU.USER_NAME", "TU.ID"};
		for (String field : fields) {
			if (where.length() > 0) {
				where.append(" OR ");
			}
			where.append(field + " LIKE '%" + keyword + "%'");
		}

		try(Statement statement = getConnection().createStatement();) {
			// SQL文を発行
			String sql = createSQL(" ("+ where.toString() + ")");
			ResultSet resultSet = statement.executeQuery(sql);
			boolean br = resultSet.next();
			if (br == false) {
				return Optional.of(new TodoTaskEntity[0]);
			}
			
			List<TodoTaskEntity> tasks = new ArrayList<TodoTaskEntity>();
			do {
				TodoTaskEntity task = new TodoTaskEntity();
				task.setId(resultSet.getString("ID"));
				task.setTask(resultSet.getString("TASK"));
				TodoUserEntity user = new TodoUserEntity();
				user.setId(resultSet.getString("ID"));
				user.setUserName(resultSet.getString("USER_NAME"));
				task.setUser(user);
				task.setExpireDate(resultSet.getDate("EXPIRE_DATE"));
				task.setFinishedDate(resultSet.getDate("FINISHED_DATE"));

				tasks.add(task);
			} while (resultSet.next());

			return Optional.of(tasks.toArray(new TodoTaskEntity[0]));
		} catch (SQLException e) {
			_pooledConnection = null;
			throw e;
		}
	}

	/**
	 * INSERT/UPDATE/DELETE文を実行します。
	 * 
	 * @param sql
	 * @return
	 * @throws ServletException
	 */
	private int executeUpdate(String sql) throws SQLException {
		try(Statement statement = getConnection().createStatement();) {
			// SQL文を発行
			
			int updateCount = statement.executeUpdate(sql);

			return updateCount;
		} catch (SQLException e) {
			_pooledConnection = null;
			throw e;
		}
	}

	/**
	 * タスク取得用のSQL文を生成します。
	 * 
	 * @param where
	 * @return
	 */
	private String createSQL(String where) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT "
						+ "TT.ID "
						+ ",TT.TASK "
						+ ", TT.USER_ID "
						+ ", TU.USER_NAME "
						+ ", TT.EXPIRE_DATE "
						+ ", TT.FINISHED_DATE "
						+ "FROM "
						+ "TODO_TASK TT "
						+ "INNER JOIN TODO_USER TU "
						+ "ON TT.USER_ID = TU.ID "
						+ "WHERE 1 = 1");
		if (where != null) {
			sb.append(" AND ");
			sb.append(where);
		}
		String ret = sb.toString();
		return ret;
	}

	/**
	 * 日付オブジェクトを取得します。
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 * @throws ServletException
	 */
	private Date getDate(Integer year, Integer month, Integer day) {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.clear();
			calendar.set(year, month - 1,day);

			return new Date(calendar.getTimeInMillis());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 追加用のSQL文を生成します。
	 * 
	 * @param targetItem
	 * @return
	 */
	private String createInsertSQL(TodoTaskEntity targetItem) {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append("TODO_TASK");
		sb.append(" (TASK,USER_ID,EXPIRE_DATE,FINISHED_DATE)");
		sb.append(" VALUES('");
		sb.append(targetItem.getTask());
		sb.append("', '");
		sb.append(targetItem.getUser().getId());
		sb.append("', '");
		sb.append(targetItem.getExpireDate().toString());
		sb.append("', null)");

		return sb.toString();
	}

	/**
	 * 更新用のSQL文を生成します。
	 * 
	 * @param targetTask
	 * @return
	 */
	private String createUpdateSQL(TodoTaskEntity targetTask) {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append("TODO_TASK");
		sb.append(" SET ");
		sb.append("TASK='");
		sb.append(targetTask.getTask());
		sb.append("', ");
		sb.append("EXPIRE_DATE='");
		sb.append(targetTask.getExpireDate().toString());
		sb.append("', ");
		sb.append("USER_ID='");
		sb.append(targetTask.getUser().getId());
		sb.append("', ");
		sb.append("FINISHED_DATE=");
		if (targetTask.getFinishedDate() != null) {
			sb.append("'");
			sb.append(targetTask.getFinishedDate().toString());
			sb.append("'");
		} else {
			sb.append("null");
		}
		sb.append(" WHERE ID='");
		sb.append(targetTask.getId());
		sb.append("'");

		return sb.toString();
	}

	/**
	 * 削除用のSQL文を生成します。
	 * 
	 * @param targetTask
	 * @return
	 */
	private String createDeleteSQL(TodoTaskEntity targetTask) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		sb.append("TODO_TASK");
		sb.append(" WHERE ID='");
		sb.append(targetTask.getId());
		sb.append("'");

		return sb.toString();
	}

}
