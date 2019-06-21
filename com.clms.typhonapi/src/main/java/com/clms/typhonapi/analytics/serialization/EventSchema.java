package com.clms.typhonapi.analytics.serialization;

import java.io.IOException;
import java.util.Map;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import com.clms.typhonapi.analytics.datatypes.events.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;

public class EventSchema implements DeserializationSchema<Event>, Deserializer,
		SerializationSchema<Event>, Serializer {
	private static ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule());

	private static Gson gson = new Gson();

	private Class<?> eventClass;

	static {
//		objectMapper.registerSubtypes(new NamedType(Delete.class));
//		objectMapper.registerSubtypes(new NamedType(Insert.class));
//		objectMapper.registerSubtypes(new NamedType(Update.class));
//		objectMapper.registerSubtypes(new NamedType(Select.class));
		
		
		SimpleModule module = new SimpleModule("StatementMappingModule", Version.unknownVersion());

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(Statement.class, Insert.class);
		resolver.addMapping(Statement.class, Select.class);
		resolver.addMapping(Statement.class, Update.class);
		resolver.addMapping(Statement.class, Delete.class);
//		resolver.addMapping(MultiPartName.class, Database.class);
		
//		
//		String json = "{\"database\":\"My database\"}";
//	     
//	    InjectableValues inject = new InjectableValues.Std()
//	      .addValue(net.sf.jsqlparser.schema.Database.class, 1);
//		try {
//			objectMapper.reader(inject)
//					.forType(net.sf.jsqlparser.schema.Database.class)
//					.readValue(json);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	    

		module.setAbstractTypes(resolver);

		objectMapper.registerModule(module);
		objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
		
		
	}

	public EventSchema() {
		// TODO Auto-generated constructor stub
	}

	public EventSchema(Class<?> eventClass) {
		this.eventClass = eventClass;
	}

	private byte[] convertObjectToByteArray(Object obj) {
		byte[] serializedDataArray = null;
		try {
			if (obj != null) {
				serializedDataArray = objectMapper.writeValueAsBytes(obj);
				// System.out.println(gson.toJson(obj));
				// serializedDataArray = gson.toJson(obj).getBytes();
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return serializedDataArray;
	}

	@Override
	public TypeInformation<Event> getProducedType() {

		return TypeExtractor.getForClass(Event.class);
	}

	@Override
	public void configure(Map configs, boolean isKey) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object deserialize(String topic, byte[] data) {
		// Kafka Deserializer
		Event event = null;
		try {
			event = deserialize(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return event;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Event deserialize(byte[] message) throws IOException {
		// Flink Deserializer

		Event event = null;
		try {

			if (message != null) {
//				event = (Event) gson.fromJson(message.toString(),
//						this.eventClass);

				 event = (Event) objectMapper
				 .readValue(message, this.eventClass);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;

	}

	@Override
	public boolean isEndOfStream(Event nextElement) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] serialize(String topic, Object data) {
		// Kafka serializer

		return convertObjectToByteArray(data);
	}

	@Override
	public byte[] serialize(Event element) {
		// Flink serializer

		return convertObjectToByteArray(element);
	}

}
