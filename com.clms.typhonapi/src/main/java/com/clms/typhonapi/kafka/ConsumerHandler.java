package com.clms.typhonapi.kafka;

@FunctionalInterface
public interface ConsumerHandler {
	void onNewMesaage(String message);
}