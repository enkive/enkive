package com.linuxbox.enkive.permissions;

import java.util.Collection;

import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.message.Message;

public interface PermissionService {

	public String getCurrentUsername();
	
	public boolean isAdmin();
	
	public Collection<String> getCurrentUserAuthorities() throws CannotGetPermissionsException;
	
	public boolean canReadMessage(String userId, Message message) throws CannotGetPermissionsException;
	
	public Collection<String> canReadAddresses(String userId);
	
}
