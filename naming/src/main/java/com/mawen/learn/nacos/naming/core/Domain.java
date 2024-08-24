package com.mawen.learn.nacos.naming.core;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/22
 */
public interface Domain {

	String getName();

	void setName(String name);

	String getToken();

	void setToken(String token);

	List<String> getOwners();

	void setOwners(List<String> owners);

	void init();

	void destroy() throws Exception;

	List<IpAddress> allIPs();

	List<IpAddress> srvIPs(String clientIP);

	String toJSON();

	void setProtectThreshold(float protectThreshold);

	float getProtectThreshold();

	void update(Domain domain);

	String getChecksum();

	void recalculateChecksum();
}
