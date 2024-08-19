package com.mawen.learn.nacos.client.logger.option;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.mawen.learn.nacos.client.logger.Level;
import com.mawen.learn.nacos.client.logger.support.LogLog;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public abstract class AbstractActiveOption implements ActivateOption {
	
	protected String productName;
	
	protected Level level;

	@Override
	public String getProductName() {
		return productName;
	}

	@Override
	public Level getLevel() {
		return level;
	}
	
	protected void setProductName(String productName) {
		if (this.productName == null && productName != null) {
			this.productName = productName;
		}
	}

	public static void invokeMethod(Object object, List<Object[]> args) {
		if (object != null && args != null) {
			for (Object[] arg : args) {
				if (arg != null && arg.length == 3) {
					Method m = null;
					try {
						m = object.getClass().getMethod((String) arg[0], (Class<?>[]) arg[1]);
						m.invoke(object, arg[2]);
					}
					catch (NoSuchMethodException e) {
						LogLog.info("Can't find method for " + object.getClass() + " " + arg[0] + " " + arg[2]);
					}
					catch (IllegalAccessException e) {
						LogLog.info("Can't invoke method for " + object.getClass() + " " + arg[0] + " " + arg[2]);
					}
					catch (InvocationTargetException e) {
						LogLog.info("Can't invoke method for " + object.getClass() + " " + arg[0] + " " + arg[2]);
					}
					catch (Throwable t) {
						LogLog.info("Can't invoke method for " + object.getClass() + " " + arg[0] + " " + arg[2]);
					}
				}
			}
		}
	}
}
