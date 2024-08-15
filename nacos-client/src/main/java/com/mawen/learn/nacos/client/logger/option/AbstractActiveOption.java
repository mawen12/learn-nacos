package com.mawen.learn.nacos.client.logger.option;

import java.util.List;

import com.mawen.learn.nacos.client.logger.Level;

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
				// TODO from here to code
			}
		}
	}
}
