package com.linuxbox.enkive.permissions;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.message.Message;

public class SpringContextPermissionService implements PermissionService {
	
	@Override
	public String getCurrentUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
	
	public boolean isAdmin(){
		Collection<String> authorityStrings = new HashSet<String>();
		for(GrantedAuthority auth : SecurityContextHolder.getContext().getAuthentication().getAuthorities()){
			authorityStrings.add(auth.getAuthority());
		}
		return authorityStrings.contains("ROLE_ENKIVE_ADMIN");
	}

	@Override
	public boolean canReadMessage(String userId, Message message)
			throws CannotGetPermissionsException {
		if(isAdmin())
			return true;
		
		Collection<String> canReadAddresses = canReadAddresses(userId);
		Collection<String> addressesInMessage = new HashSet<String>();
		addressesInMessage.addAll(message.getTo());
		addressesInMessage.addAll(message.getCc());
		addressesInMessage.addAll(message.getBcc());
		addressesInMessage.addAll(message.getFrom());
		addressesInMessage.add(message.getMailFrom());
		addressesInMessage.addAll(message.getRcptTo());
		
		return CollectionUtils.containsAny(addressesInMessage, canReadAddresses);
		
	}

	@Override
	public Collection<String> canReadAddresses(String userId) {
		Collection<String> addresses = new HashSet<String>();
		addresses.add(getCurrentUsername());
		return addresses;
	}

	@Override
	public Collection<String> getCurrentUserAuthorities()
			throws CannotGetPermissionsException {
		Collection<String> authorityStrings = new HashSet<String>();
		for(GrantedAuthority auth : SecurityContextHolder.getContext().getAuthentication().getAuthorities()){
			authorityStrings.add(auth.getAuthority());
		}
		return authorityStrings;
	}
}
