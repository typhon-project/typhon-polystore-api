package com.clms.typhonapi.analytics.datatypes.commands;

import java.util.ArrayList;

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("select")
public class SelectCommand extends DMLCommand {

	// private Select statement;
	//
	// public SelectCommand() {
	// }
	//
	// public SelectCommand(String sql) {
	// statement = (Select) super.parseSqlStatement(sql);
	// }
	//
	// public Select getStatement() {
	// return statement;
	// }
	//
	// public void setStatement(Statement statement) {
	// this.statement = (Select) statement;
	// }
	//
	// @Override
	// public String toString() {
	// return "SelectCommand [statement=" + statement + "]";
	// }

	ArrayList<Entity> returnedEntities;

	public ArrayList<Entity> getReturnedEntities() {
		return returnedEntities;
	}

	public void setReturnedEntities(ArrayList<Entity> returnedEntities) {
		this.returnedEntities = returnedEntities;
	}

	public void populateFromSqlStatement(String sql) {
		// Use this function to populate Select related fields

		Select statement = (Select) this.populatePilesFromSqlStatement(sql);
		PlainSelect ps = (PlainSelect) statement.getSelectBody();
		this.clause = ps.getWhere().toString();

		// Select statement =null;
		// try {
		// statement = (Select)CCJSqlParserUtil.parse(sql);
		// } catch (JSQLParserException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// System.out.println(ps.getWhere().toString());
		// System.out.println(ps.getSelectItems().get(1).toString());

		// AndExpression e = (Eq AndExpression) ps.getWhere();
		// System.out.println(e.getLeftExpression());

		// statement.get
		// PlainSelect pl = (PlainSelect)statement.getSelectBody();
		// SimpleNode exp = pl.getWhere().getASTNode();
		//
		// for (SelectItem item : pl.getSelectItems()) {
		// System.out.println(item.toString());
		// }

	}

	//
	// @Override
	// void parseStatement(String sql) {
	// statement = (Select) super.parseSqlStatement(sql);
	//
	// }

	@Override
	public String toString() {
		return "SelectCommand [returnedEntities=" + returnedEntities
				+ ", piles=" + piles + ", columns=" + columns + ", clause="
				+ clause + ", targetDb=" + targetDb + "]";
	}

}