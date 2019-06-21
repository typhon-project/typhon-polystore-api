package com.clms.typhonapi.kafka;

import com.clms.typhonapi.analytics.datatypes.events.Event;

@FunctionalInterface
public interface ConsumerHandler {
	void onNewMesaage(Event event);
}