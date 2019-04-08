import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class MySQListener implements PropertyChangeListener {
	
	/** Contiene los datos de las consultas ejecutadas con MySQLConnector */
	private ArrayList<QueryData> info = new ArrayList<QueryData>();
	
	public MySQListener() {
		
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue().getClass().equals(QueryData.class)) {
			QueryData data = (QueryData) evt.getNewValue();
			System.out.println(data.toString());
			info.add(data);
		}
	}
	
	public ArrayList<QueryData> getInfo() {
		return info;
	}

}
