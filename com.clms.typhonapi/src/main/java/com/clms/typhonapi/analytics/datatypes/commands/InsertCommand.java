package com.clms.typhonapi.analytics.datatypes.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.insert.Insert;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("insert")
public class InsertCommand extends DMLCommand {

	// private Insert statement;
	//
	// public InsertCommand() {
	// }
	//
	// public InsertCommand(String sql) {
	// statement = (Insert) super.parseSqlStatement(sql);
	// }
	//
	// public Insert getStatement() {
	// return statement;
	// }
	//
	// @Override
	// public void setStatement(Statement statement) {
	// this.statement = (Insert) statement;
	// }
	//
	// @Override
	// public String toString() {
	// return "InsertCommand [statement=" + statement + "]";
	// }

	ArrayList<Entity> insertedEntities;
	Map<String, String> columnValueMap;

	public ArrayList<Entity> getInsertedEntities() {
		return insertedEntities;
	}

	public void setInsertedEntities(ArrayList<Entity> insertedEntities) {
		this.insertedEntities = insertedEntities;
	}

	public Map<String, String> getColumnValueMap() {
		return columnValueMap;
	}

	public void setColumnValueMap(Map<String, String> columnValueMap) {
		this.columnValueMap = columnValueMap;
	}

	@Override
	public String toString() {
		return "InsertCommand [insertedEntities=" + insertedEntities
				+ ", columnValueMap=" + columnValueMap + ", piles=" + piles
				+ ", columns=" + columns + ", clause=" + clause + ", targetDb="
				+ targetDb + "]";
	}

	@Override
	public void populateFromSqlStatement(String sql) {
		// TODO Auto-generated method stub
		
	}

}
