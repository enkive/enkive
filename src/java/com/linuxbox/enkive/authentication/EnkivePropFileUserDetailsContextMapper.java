package com.linuxbox.enkive.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import com.linuxbox.enkive.normalization.EmailAddressNormalizer;

public class EnkivePropFileUserDetailsContextMapper implements
		UserDetailsService, ApplicationContextAware, InitializingBean {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.authentication");

	protected InMemoryUserDetailsManager delegateUserDetailsManager;
	protected ApplicationContext applicationContext;
	protected EmailAddressNormalizer emailAddressNormalizer;

	protected String defaultDomain;
	protected String properties;
	protected String userAddresses;
	
	private static Properties userAddressesProps;

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		final UserDetails plainDetails = delegateUserDetailsManager
				.loadUserByUsername(username);
		final EnkiveUserDetails enkiveDetails = new EnkiveUserDetails(
				plainDetails, emailAddressNormalizer);
		
		// add any addresses listed explicitly in addresses.properties
		enkiveDetails.addFromProperties(username, userAddressesProps);

		String emailAddress;
		if (username.contains("@")) {
			emailAddress = username;
		} else if (defaultDomain == null) {
			LOGGER.warn("user id is not an email address and no default domain has been set");
			emailAddress = username;
		} else {
			emailAddress = username + '@' + defaultDomain;
		}

		// add default address (if not already in the set)
		enkiveDetails.addKnownEmailAddress(emailAddress);

		return enkiveDetails;
	}

	@Override
	public void afterPropertiesSet() throws IOException {
		final Properties userProperties = new Properties();
		final Resource propResource = applicationContext
				.getResource(properties);
		final InputStream propStream = propResource.getInputStream();
		userProperties.load(propStream);
		propStream.close();

		delegateUserDetailsManager = new InMemoryUserDetailsManager(
				userProperties);
		
		// now get userAddresses
		final Resource uaResource = applicationContext.getResource(userAddresses);
		userAddressesProps = new Properties();
		final InputStream uaStream = uaResource.getInputStream();
		userAddressesProps.load(uaStream);
		uaStream.close();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public String getDefaultDomain() {
		return defaultDomain;
	}

	public void setDefaultDomain(String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}
	
	public String getUserAddresses() {
		return userAddresses;
	}

	public void setUserAddresses(String userAddresses) {
		this.userAddresses = userAddresses;
	}	

	public String getProperties() {
		return properties;
	}

	@Required
	public void setProperties(String properties) {
		this.properties = properties;
	}

	@Required
	public void setEmailAddressNormalizer(
			EmailAddressNormalizer emailAddressNormalizer) {
		this.emailAddressNormalizer = emailAddressNormalizer;
	}
}
