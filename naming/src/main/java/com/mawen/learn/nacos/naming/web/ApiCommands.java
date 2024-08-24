package com.mawen.learn.nacos.naming.web;

import com.mawen.learn.nacos.naming.misc.UtilsAndCommons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/22
 */
@RestController
@RequestMapping(UtilsAndCommons.NACOS_NAMING_CONTEXT + "/api")
public class ApiCommands {

	@Autowired
	private DomainsManager domainsManager;


}
