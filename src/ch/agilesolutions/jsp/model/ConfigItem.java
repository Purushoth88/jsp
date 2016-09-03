package ch.agilesolutions.jsp.model;

public class ConfigItem {
	
	private String filename;
	
	private String unixName;
	
	private String windowsName;
	
	public ConfigItem(String filename, String unixName, String windowsName) {
		
		this.filename = filename;
		this.unixName = unixName;
		this.windowsName = windowsName;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getUnixName() {
		return unixName;
	}

	public void setUnixName(String unixName) {
		this.unixName = unixName;
	}

	public String getWindowsName() {
		return windowsName;
	}

	public void setWindowsName(String windowsName) {
		this.windowsName = windowsName;
	}
	
	

}
