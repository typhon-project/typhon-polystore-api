package com.clms.typhonapi.analytics.datatypes.events;

import java.io.Serializable;
import java.util.Date;

import com.clms.typhonapi.analytics.datatypes.commands.DMLCommand;
import com.clms.typhonapi.analytics.datatypes.commands.DeleteCommand;
import com.clms.typhonapi.analytics.datatypes.commands.InsertCommand;
import com.clms.typhonapi.analytics.datatypes.commands.SelectCommand;
import com.clms.typhonapi.analytics.datatypes.commands.UpdateCommand;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class PreEvent extends Event implements Serializable {

	String user;
	Date queryTime;
	String dbUser;
	boolean authenticated;

	public PreEvent() {
		super();
	}

	public PreEvent(String id, String query, String user, Date queryTime,
			String dbUser) {
		super(id, query);
		this.user = user;
		this.queryTime = queryTime;
		this.dbUser = dbUser;
		this.authenticated = true;
//		try {
//			this.dmlCommand = CommandFactory.getInstance(query);
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	public String getUser() {
		return user;
	}

	public Date getQueryTime() {
		return queryTime;
	}

	public String getDbUser() {
		return dbUser;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setQueryTime(Date queryTime) {
		this.queryTime = queryTime;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	@Override
	public String toString() {
		return "PreEvent [user=" + user + ", queryTime=" + queryTime
				+ ", dbUser=" + dbUser + ", authenticated=" + authenticated
				+ ", id=" + id + ", query="
				+ query + "]";
	}

}
