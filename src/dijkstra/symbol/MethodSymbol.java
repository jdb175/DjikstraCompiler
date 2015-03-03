package dijkstra.symbol;

import java.util.ArrayList;
import java.util.List;

import dijkstra.utility.DijkstraType;

/**
 * Class for a method symbol, which has a list of parameter types
 * @author Jason Whitehouse
 */
public class MethodSymbol extends Symbol {
	List<DijkstraType> parameterList;

	public MethodSymbol(String id, DijkstraType type, boolean isLocal) {
		super(id, type, isLocal);
		this.parameterList = new ArrayList<DijkstraType>();
	}
	
	/**
	 * adds given parameter to end of list of parameters for this method
	 * @param param
	 */
	public void addParameter (DijkstraType param) {
		parameterList.add(param);
	}
	
	/**
	 * @param index
	 * @return the parameter at given index in list of parameters
	 */
	public DijkstraType getParameter(int index) {
		return parameterList.get(index);
	}
	
	/**
	 * @return the length of the parameter list
	 */
	public int getParameterListLength() {
		return parameterList.size();
	}
	
	
	public String getName() {
		if(this.getType() == DijkstraType.PROCEDURE) {
			return "p"+this.getId();
		} else {
			return "f"+this.getId();
		}
	}
	
	public String getSignature() {
		StringBuilder str = new StringBuilder();
		str.append("(");
		for(int i = 0; i < parameterList.size(); i++) {
			str.append(convertType(parameterList.get(i)));
		}
		str.append(")");
		str.append(convertType(this.getType()));
		return str.toString();
	}
}
