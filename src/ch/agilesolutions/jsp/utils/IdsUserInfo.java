package ch.agilesolutions.jsp.utils;

import com.jcraft.jsch.UserInfo;

public class IdsUserInfo implements UserInfo {

	@Override
	public void showMessage(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean promptYesNo(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean promptPassword(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean promptPassphrase(String arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassphrase() {
		// TODO Auto-generated method stub
		return null;
	}

}
