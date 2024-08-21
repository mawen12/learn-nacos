package com.mawen.learn.nacos.client.config.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class EventDispatcher {

	private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);

	public static final Map<Class<? extends AbstractEvent>, CopyOnWriteArrayList<AbstractEventListener>> LISTENER_MAP = new ConcurrentHashMap<>();

	public static void addEventListener(AbstractEventListener listener) {
		for (Class<? extends AbstractEvent> type : listener.interest()) {
			getListenerList(type).addIfAbsent(listener);
		}
	}

	public static void fireEvent(AbstractEvent abstractEvent) {
		if (null == abstractEvent) {
			return;
		}

		for (AbstractEvent implyEvent : abstractEvent.implyEvents()) {
			try {
				if (abstractEvent != implyEvent) {
					fireEvent(implyEvent);
				}
			}
			catch (Exception e) {
				log.warn(e.toString(), e);
			}
		}

		for (AbstractEventListener listener : getListenerList(abstractEvent.getClass())) {
			try {
				listener.onEvent(abstractEvent);
			}
			catch (Exception e) {
				log.warn(e.toString(), e);
			}
		}
	}

	static synchronized CopyOnWriteArrayList<AbstractEventListener> getListenerList(Class<? extends AbstractEvent> eventType) {
		CopyOnWriteArrayList<AbstractEventListener> listeners = LISTENER_MAP.get(eventType);
		if (null == listeners) {
			listeners = new CopyOnWriteArrayList<>();
			LISTENER_MAP.put(eventType, listeners);
		}
		return listeners;
	}

	public static abstract class AbstractEvent {

		protected List<AbstractEvent> implyEvents() {
			return Collections.EMPTY_LIST;
		}
	}

	public static abstract class AbstractEventListener {

		public AbstractEventListener() {
			EventDispatcher.addEventListener(this);
		}

		public abstract List<Class<? extends AbstractEvent>> interest();
		public abstract void onEvent(AbstractEvent abstractEvent);

	}

	public static class ServerlistChangeEvent extends AbstractEvent {

	}
}
