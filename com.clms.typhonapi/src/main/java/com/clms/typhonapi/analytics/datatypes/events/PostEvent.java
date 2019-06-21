package com.clms.typhonapi.analytics.datatypes.events;

import java.util.Date;

public class PostEvent extends Event {
	private Boolean success;
	private Date startTime;
	private Date endTime;
	private PreEvent preEvent;



	public PostEvent() {
		super();
	}
	
	public PostEvent(String id, String query, Boolean success, Date startTime,
			Date endTime, PreEvent preEvent) {
		super(id, query);
		this.success = success;
		this.startTime = startTime;
		this.endTime = endTime;
		this.preEvent = preEvent;
		;
	}

//	public PostEvent(String id, String query, Boolean success, Date startTime,
//			Date endTime, PreEvent preEvent, DMLCommand dmlCommand) {
//		super(id, query);
//		this.success = success;
//		this.startTime = startTime;
//		this.endTime = endTime;
//		this.preEvent = preEvent;
//		this.dmlCommand = dmlCommand;
//	}

	public Boolean getSuccess() {
		return success;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public PreEvent getPreEvent() {
		return preEvent;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setPreEvent(PreEvent preEvent) {
		this.preEvent = preEvent;
	}

	@Override
	public String toString() {
		return "PostEvent [success=" + success + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", preEvent=" + preEvent + ", id="
				+ id + ", query=" + query + "]";
	}

//	@Override
//	public String toString() {
//		return "PostEvent [success=" + success + ", startTime=" + startTime
//				+ ", endTime=" + endTime + ", preEvent=" + preEvent
//				+ ", dmlCommand=" + dmlCommand + ", id=" + id + ", query="
//				+ query + "]";
//	}

}
