package com.clms.typhonapi.kafka;

import ac.york.typhon.analytics.commons.datatypes.events.Event;

@FunctionalInterface
public interface ConsumerHandler {
	void onNewMesaage(Event event);
}