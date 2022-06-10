package test.entity;

import java.sql.Date;

/**
 * タスク情報を保持します。
 */
public class TodoTaskEntity {

	/**
	 * IDを保持します。
	 */
	private String id;
	
	/**
	 * タスクを保持します。
	 */
	private String task;
	
	/**
	 * 担当者を保持します。
	 */
	private TodoUserEntity user;
	
	/**
	 * 期限を保持します。
	 */
	private Date expireDate;
	
	/**
	 * 終了した日時を保持します。
	 */
	private Date finishedDate;
	
	
	
	/**
	 * 構築します。
	 */
	public TodoTaskEntity() {
	}
	
	/**
	 * IDを取得します。
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * IDを設定します。
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * タスクを取得します。
	 * @return
	 */
	public String getTask() {
		return task;
	}

	/**
	 * タスクを設定します。
	 * @param name
	 */
	public void setTask(String task) {
		this.task = task;
	}
	
	/**
	 * ユーザを取得します。
	 * @return
	 */
	public TodoUserEntity getUser() {
		return user;
	}

	/**
	 * ユーザを設定します。
	 * @param user
	 */
	public void setUser(TodoUserEntity user) {
		this.user = user;
	}
	
	/**
	 * 期限を取得します。
	 * @return
	 */
	public Date getExpireDate() {
		return expireDate;
	}
	
	/**
	 * 期限を設定します。
	 * @param expireDate
	 */
	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
	
	/**
	 * 終了日時を取得します。
	 * @return
	 */
	public Date getFinishedDate() {
		return finishedDate;
	}
	
	/**
	 * 終了日時を設定します。
	 * @param finishedDate
	 */
	public void setFinishedDate(Date finishedDate) {
		this.finishedDate = finishedDate;
	}
}
