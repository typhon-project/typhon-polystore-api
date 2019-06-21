package com.clms.typhonapi.analytics.enums;

public interface ITopicType {

	public default String getLabel(){
		return ((Enum<?>)this).name().toLowerCase();
		
	}
}