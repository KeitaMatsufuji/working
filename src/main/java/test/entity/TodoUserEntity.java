package test.entity;

/**
 * ユーザ情報を保持します。
 */
public class TodoUserEntity {
	
	/**
	 * IDを保持します。
	 */
	private String id;
	
	/**
	 * 名前を保持します。
	 */
	private String userName;

	
	
	/**
	 * 構築します。
	 */
	public TodoUserEntity() {
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
	 * 名前を取得します。
	 * @return
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * 名前を設定します。
	 * @param name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
}
