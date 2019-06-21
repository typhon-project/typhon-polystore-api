package com.clms.typhonapi.analytics.datatypes.commands;

import java.util.ArrayList;

import net.sf.jsqlparser.statement.delete.Delete;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("delete")
public class DeleteCommand extends DMLCommand {

	// private Delete statement;

//	public DeleteCommand() {
//
//	}
//
//	public DeleteCommand(String sql) {
//		// statement = (Delete) super.parseSqlStatement(sql);
//	}

	// public Delete getStatement() {
	// return statement;
	// }
	//
	// public void setStatement(Statement statement) {
	// this.statement = (Delete) statement;
	// }

	// @Override
	// public String toString() {
	// return "DeleteCommand [statement=" + statement + "]";
	// }

	ArrayList<Entity> deletedEntities;

	public ArrayList<Entity> getDeletedEntities() {
		return deletedEntities;
	}

	public void setDeletedEntities(ArrayList<Entity> deletedEntities) {
		this.deletedEntities = deletedEntities;
	}

	@Override
	public void populateFromSqlStatement(String sql) {
		// Use this function to populate Delete related fields

		Delete statement = (Delete) this.populatePilesFromSqlStatement(sql);
		// System.out.println("Event: Delete  " + this.piles);

		this.clause = statement.getWhere().toString();

	}

	@Override
	public String toString() {
		return "DeleteCommand [deletedEntities=" + deletedEntities + ", piles="
				+ piles + ", columns=" + columns + ", clause=" + clause
				+ ", targetDb=" + targetDb + "]";
	}

}
