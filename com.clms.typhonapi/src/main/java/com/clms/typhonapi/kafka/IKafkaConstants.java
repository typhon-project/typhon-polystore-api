package com.clms.typhonapi.kafka;

public interface IKafkaConstants {
    public static Integer MESSAGE_COUNT=2;
    public static String CLIENT_ID="client1";
    public static String TOPIC_NAME="PRE";
    public static String AUTH_TOPIC="AUTH";
    public static String GROUP_ID_CONFIG="consumerGroup1";
    public static Integer MAX_NO_MESSAGE_FOUND_COUNT=100;
    public static String OFFSET_RESET_LATEST="latest";
    public static String OFFSET_RESET_EARLIER="earliest";
    public static Integer MAX_POLL_RECORDS=1;
}
