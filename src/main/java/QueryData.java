/**
 * Clase para guardar los datos de la consulta
 */
public class QueryData {
	private String user;
	private String db_name;
	private String executedQuery;
	private String dateExecuted;
	private int returned_rows;
	private String type;
	
	public QueryData() {
		
	}
	
	public QueryData(String user, String db_name, String executedQuery, String dateExecuted, int returned_rows,
			String type) {
		this.user = user;
		this.db_name = db_name;
		this.executedQuery = executedQuery;
		this.dateExecuted = dateExecuted;
		this.returned_rows = returned_rows;
		this.type = type;
	}
	
	// Getters
	public String getUser() {
		return user;
	}

	public String getDb_name() {
		return db_name;
	}

	public String getExecutedQuery() {
		return executedQuery;
	}

	public String getDateExecuted() {
		return dateExecuted;
	}

	public int getReturned_rows() {
		return returned_rows;
	}

	public String getType() {
		return type;
	}
	
	// Setters
	public void setUser(String user) {
		this.user = user;
	}

	public void setDb_name(String db_name) {
		this.db_name = db_name;
	}

	public void setExecutedQuery(String executedQuery) {
		this.executedQuery = executedQuery;
	}

	public void setDateExecuted(String dateExecuted) {
		this.dateExecuted = dateExecuted;
	}

	public void setReturned_rows(int returned_rows) {
		this.returned_rows = returned_rows;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "QueryData [user=" + user + ", db_name=" + db_name + ", executedQuery=" + executedQuery
				+ ", dateExecuted=" + dateExecuted + ", returned_rows=" + returned_rows + ", type=" + type + "]";
	}
	
	protected String dataFromQuery1() {
		return "executedQuery=" + executedQuery + ", dateExecuted=" + dateExecuted + ", type=" + type;
	}
	
	protected String dataFromQuery2() {
		return "executedQuery=" + executedQuery + ", dateExecuted=" + dateExecuted;
	}
	
	protected String dataFromQuery3() {
		return "executedQuery=" + executedQuery + ", dateExecuted=" + dateExecuted + ", user=" + user;
	}
	
}
