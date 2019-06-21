package com.clms.typhonapi.analytics.datatypes.events;

import com.clms.typhonapi.analytics.enums.StatementType;

public abstract class Event {

	protected String id;
	protected String query;

	public Event() {

	}

	public Event(String id, String query) {

		this.id = id;
		this.query = query;
	}

	public StatementType retrieveStatementType() {

		// TODO use Regex instead of split
//		System.out.println("getStatementType ############################ got called ");
		return StatementType.valueOf(query.split(" ")[0].toUpperCase().trim());

	}

	public String getId() {
		return id;
	}

	public String getQuery() {
		return query;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public String toString() {
		return "Event [id=" + id + ", query=" + query + "]";
	}

}
