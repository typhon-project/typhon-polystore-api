package com.clms.typhonapi.analytics.datatypes.commands;

import java.util.ArrayList;

import net.sf.jsqlparser.statement.update.Update;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("update")
public class UpdateCommand extends DMLCommand {

	// private Update statement;
	//
	// public UpdateCommand() {
	//
	// }
	//
	// public UpdateCommand(String sql) {
	// statement = (Update) super.parseSqlStatement(sql);
	// }
	//
	// public Update getStatement() {
	// return statement;
	// }
	//
	// public void setStatement(Statement statement) {
	// this.statement = (Update) statement;
	// }
	//
	// @Override
	// public String toString() {
	// return "UpdateCommand [statement=" + statement + "]";
	// }

	ArrayList<Entity> updatedEntities, oldEntities;

	public ArrayList<Entity> getUpdatedEntities() {
		return updatedEntities;
	}

	public ArrayList<Entity> getOldEntities() {
		return oldEntities;
	}

	public void setUpdatedEntities(ArrayList<Entity> updatedEntities) {
		this.updatedEntities = updatedEntities;
	}

	public void setOldEntities(ArrayList<Entity> oldEntities) {
		this.oldEntities = oldEntities;
	}

	@Override
	public void populateFromSqlStatement(String sql) {
		// Use this function to populate Update related fields

		Update statement = (Update) this.populatePilesFromSqlStatement(sql);
		// System.out.println("Event: Update  " + this.piles);

		this.clause = statement.getWhere().toString();

	}

	@Override
	public String toString() {
		return "UpdateCommand [updatedEntities=" + updatedEntities
				+ ", oldEntities=" + oldEntities + ", piles=" + piles
				+ ", columns=" + columns + ", clause=" + clause + ", targetDb="
				+ targetDb + "]";
	}
}
